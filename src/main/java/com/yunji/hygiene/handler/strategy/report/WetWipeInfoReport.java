package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.domain.resp.report.WetWipeInfoReportResp;
import com.yunji.hygiene.entity.dto.DeviceCellDetailDTO;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.dto.WipeDeviceInfoDTO;
import com.yunji.hygiene.entity.enums.ContainerTypeEnum;
import com.yunji.hygiene.entity.po.ContainerCellPO;
import com.yunji.hygiene.entity.po.ContainerPO;
import com.yunji.hygiene.entity.po.ProductPO;
import com.yunji.hygiene.handler.calculate.CabinetCalculate;
import com.yunji.hygiene.handler.convert.DeviceConvert;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceInfoCache;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * 湿纸巾设备状态上报处理器（Report 策略实现）
 *
 * 该类处理的典型场景：
 * - 设备周期性上报自身状态（电量/睡眠/锁状态/限位/信号强度/纸巾传感器状态等）
 * - 设备在执行平台下发指令后（例如开门、关门、升级等），也会在上报中携带 eventId，
 *   用于“指令执行结果回执/闭环”，平台据此更新 event 表、标记事件完成等。
 *
 * 该处理器做的核心事情（按顺序）：
 * 1）解析上报消息，转换成业务 DTO（WipeDeviceInfoDTO）
 * 2）如果携带 eventId：判断是否在“事件有效时间窗”内，决定是否更新事件、是否完成事件
 * 3）根据 sleepStatus 写/删 Redis 设备睡眠 key（device:sleep:{imei}）
 * 4）根据 imei 查询柜子（ContainerPO），更新柜子实时状态字段（电量/锁/限位/RSSI...）
 * 5）查询柜子格子（湿纸巾通常只有一个格子/或主格子），结合商品与型号高度计算“事件数量”
 * 6）做“系统库存(productQuantity) vs 设备传感器(tissueStatus)”一致性校验，
 *    异常则标记 runtimeStatus=0 并触发通知（noticeImei type=4）
 * 7）落库（更新格子与柜子）
 * 8）更新缓存（DeviceInfoCache）
 * 9）返回终端通用应答（CommonResp.success）
 */
@Slf4j
@Service
public class WetWipeInfoReport extends AbsTranReportMsg {

    /**
     * 事件有效时间窗（10分钟）
     *
     * 设备上报里带 eventId 时，平台会查询 event 的最后更新时间 updateTime：
     * - 如果当前上报时间 - updateTime < EVENT_DIFF_TIME，则认为该上报属于“最近一次指令的闭环回执”
     *   => 允许 updateEvent / eventFinish
     * - 超过窗口，则认为是历史/过期回执，避免污染最新事件状态
     */
    private static final long EVENT_DIFF_TIME = 10 * 60 * 1000;

    /**
     * 设备睡眠 key 的 TTL（单位：HOURS）
     * - sleepStatus==1：写入 Redis，TTL=2000小时（约83天），代表“设备处于睡眠”
     * - sleepStatus!=1：删除 Redis key，代表“设备唤醒”
     */
    private static final int SLEEP_HOURS = 2000;

    /**
     * 运行异常描述（用于柜子运行状态字段）
     * - 当系统库存(productQuantity) 与 设备探测(tissueStatus) 出现矛盾时写入
     */
    private static final String ERROR_EXCEPTION = "商品数量异常";

    /**
     * 处理设备上报（湿纸巾信息上报）
     *
     * @param ctx Netty 上下文
     * @param msg 已解析后的 ReportMsg（具体类型为 WetWipeInfoReportResp）
     * @return TransReportDTO（包含：是否继续处理/是否需要应答/应答包）
     */
    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {

        // 是否需要更新事件（event 表 afterCmd/updateTime）
        boolean updateEvent = false;
        // 是否需要完成事件（event 表 finishTime/状态）
        boolean eventFinish = false;

        // 记录原始消息（注意：msg.toJson 可能很大，生产可考虑降级到 debug 或抽样）
        log.info("WetWipeInfoReport channelRead0 msg:{}", JsonUtil.toJsonString(msg));

        // 设备唯一标识（来自 JT808 Header 的 imei）
        String imei = msg.getHeader().getImei();

        // 强转为具体的湿纸巾上报响应对象（内部持有 payload ByteBuf 并实现 parseBody）
        WetWipeInfoReportResp sysInfo = (WetWipeInfoReportResp) msg;

        // 把协议对象转换成业务 DTO，后续全部使用 devInfo 进行业务判断与落库
        WipeDeviceInfoDTO devInfo = DeviceConvert.convert(sysInfo);

        // ----------------------- 1）事件闭环：是否更新事件 / 是否完成事件 -----------------------
        // 设备可能携带 eventId 表示它在执行某条平台下发指令（开门/关门/升级/查询等）的结果回执
        if (devInfo.getEventId() != null && devInfo.getEventId() > 0) {

            // 查询 event 最近更新时间（通常是 event 表 updateTime 或 afterCmd 更新时间）
            Date updateTime = deviceService.getUpdateTime(devInfo.getEventId());

            // 注意：你这里 updateTime 为空时用 new Date()，会导致 diffTime≈0，必然进入有效窗口
            // 这可能会让“不存在/异常的 eventId”也被当成有效事件更新。
            // 更严谨写法：updateTime==null 直接不走 updateEvent；或在这里做 event 存在性校验。
            updateTime = updateTime == null ? new Date() : updateTime;

            // 计算当前上报与事件更新时间的时间差
            long diffTime = System.currentTimeMillis() - updateTime.getTime();

            // 在有效时间窗内，认为该上报是“与该 eventId 强相关的回执”
            if (diffTime < EVENT_DIFF_TIME) {
                log.debug("WetWipeInfoReport channelRead0 diffTime:{}", diffTime);

                // 标记：需要把 devInfo 写入 event.afterCmd（用于后台查看指令执行结果）
                updateEvent = true;

                // 完成事件的判定：
                // - 门全部关上（inLimitStatus==1：入门限位/关门到位）
                // - 并且锁住（lockStatus==1）
                // 满足则认为“该事件指令已执行完成”
                if (devInfo.getInLimitStatus() == 1 && devInfo.getLockStatus() == 1) {
                    eventFinish = true;
                }
            }
        }

        // ----------------------- 2）睡眠状态缓存：写/删 Redis key -----------------------
        // sleepStatus==1：写 key device:sleep:{imei}，并设置较长 TTL
        if (devInfo.getSleepStatus() == 1) {
            SystemUtil.redisCache.set(
                    DeviceCacheCode.DEVICE_SLEEP + imei,
                    new Date(),        // value：记录写入时间，便于排查（不是必须）
                    SLEEP_HOURS,       // TTL 时间
                    HOURS             // TTL 单位
            );
            log.info("WetWipeInfoReport sleep imei {}", imei);
        } else {
            // 非睡眠：删除 key
            SystemUtil.redisCache.delete(DeviceCacheCode.DEVICE_SLEEP + imei);
            log.info("WetWipeInfoReport wakeup imei {}", imei);
        }

        // ----------------------- 3）查询柜子并更新柜子实时状态字段 -----------------------
        // 根据 imei 找到柜子主表记录（ContainerPO）
        ContainerPO containerPO = deviceService.findByChipImei(imei);
        if (containerPO != null) {

            // 电量状态：battleStatus（你们定义：1=正常? 0=低电?，从逻辑推断）
            Integer battleStatus = containerPO.getBattleStatus();

            // 阈值翻转告警逻辑：
            // - 之前是低电/告警态(battleStatus==1?)，现在电量<=30 => 切到另一种状态并通知
            // - 之前是正常态(battleStatus==0?)，现在电量>30 => 恢复
            //  注意：battleStatus 的语义你们要确认，这里从代码推断是“>30 正常，<=30 低电”
            if (battleStatus == 1 && devInfo.getBattleLevel() <= 30) {
                // 进入低电状态
                containerPO.setBattleStatus(0);
                containerPO.setUpdateBattleTime(new Date());

                // 触发通知：type=7（推测为低电量通知）
                deviceService.noticeImei(containerPO.getChipImei(), 7);
            } else if (battleStatus == 0 && devInfo.getBattleLevel() > 30) {
                // 恢复正常状态
                containerPO.setBattleStatus(1);
                containerPO.setUpdateBattleTime(new Date());
            }

            // 同步上报的实时字段到柜子主表
            containerPO.setBattleLevel(devInfo.getBattleLevel());
            containerPO.setSleepStatus(devInfo.getSleepStatus());
            containerPO.setLockStatus(devInfo.getLockStatus());
            containerPO.setInLimitStatus(devInfo.getInLimitStatus());
            containerPO.setOutLimitStatus(devInfo.getOutLimitStatus());
            containerPO.setRssi(devInfo.getRssi());

            // ----------------------- 4）查询格子并更新格子/一致性校验（重点） -----------------------
            // 湿纸巾柜通常会有一个主要格子（或固定格子），这里取 getCell(containerId)
            ContainerCellPO cellPO = deviceService.getCell(containerPO.getId());
            if (cellPO != null) {

                // 格子绑定的商品信息（用于计算高度/库存规则/名称等）
                ProductPO product = deviceService.getProduct(cellPO.getProductId());

                // 柜型高度配置（湿纸巾柜使用 WIPE typeCode）
                BigDecimal typeHeight = deviceService.getTypeHeight(ContainerTypeEnum.WIPE.getTypeCode());

                // 计算“事件数量/库存变化/设备数量”等信息
                // 参数说明（推测）：
                // - cellPO：格子当前系统状态（库存、上限、设备数量、距离等）
                // - null：这里明确说明“湿纸巾不拿顶部红外的值”，所以第二个参数传 null（可能是红外/距离数据）
                // - typeHeight：柜型高度，用于把距离/高度换算成数量
                // - product：商品配置（单包厚度/高度、上限、换算规则等）
                DeviceCellDetailDTO eventQuantity = CabinetCalculate.getEventQuantity(cellPO, null, typeHeight, product);

                // 补充字段：商品名（便于展示/日志/缓存）
                eventQuantity.setProductName(product.getProductName());

                // 设备上报的纸巾状态（tissueStatus）：这里直接作为“设备数量/设备检测结果”
                // 注意：tissueStatus 的语义要非常明确：
                // - 0/1 是否代表“有/无货”？
                // - 还是代表“传感器检测状态”？
                // 你这里把它写入 deviceQuantity，会影响格子表 device_quantity 字段含义。
                eventQuantity.setDeviceQuantity(devInfo.getTissueStatus());

                // 将设备检测结果写回格子记录（device_quantity）
                cellPO.setDeviceQuantity(eventQuantity.getDeviceQuantity());

                // 落库更新格子（只更新 deviceQuantity 或更多字段取决于 repo 的 save 行为）
                deviceService.updateCell(cellPO);

                // 把系统库存数量写回 devInfo（用于缓存与事件 afterCmd）
                devInfo.setProductQuantity(cellPO.getProductQuantity());

                // 将 eventQuantity 等格子信息合并进 devInfo（比如商品名、系统/设备数量、告警信息等）
                DeviceConvert.setCellMsg(devInfo, eventQuantity);

                // ----------------------- 5）一致性校验：系统库存 vs 设备探测 -----------------------
                // 校验规则：
                // 1) 系统库存 >0，但设备探测=0 => “系统认为有货，但设备认为没货”
                // 2) 系统库存 =0，但设备探测=1 => “系统认为没货，但设备认为有货”
                // 满足任一则认为“商品数量异常”，写运行异常并通知
                if ((devInfo.getProductQuantity() > 0 && devInfo.getTissueStatus() == 0) ||
                        (devInfo.getProductQuantity() == 0 && devInfo.getTissueStatus() == 1)) {

                    // 柜子运行异常
                    containerPO.setRuntimeStatus(0);
                    containerPO.setRuntimeError(ERROR_EXCEPTION);

                    // 触发异常通知：type=4（推测为异常告警）
                    deviceService.noticeImei(containerPO.getChipImei(), 4);
                } else {
                    // 运行正常
                    containerPO.setRuntimeStatus(1);
                    containerPO.setRuntimeError("");
                }

            } else {
                // 柜子存在但格子不存在：属于数据异常（初始化缺失/绑定关系断裂等）
                log.error("WetWipeInfoReport cellPO not exist id:{}", containerPO.getId());
            }

            // 更新柜子主表（battle/sleep/lock/limit/rssi/runtime...）
            deviceService.updateCabinet(containerPO);
        }

        // ----------------------- 6）事件表更新 / 完成 / 异常开门记录 -----------------------
        // 如果在事件有效窗口内，把最新 devInfo 写入 event.afterCmd（用于“事件执行结果”）
        if (updateEvent) {
            deviceService.updateEvent(devInfo.getEventId(), JsonUtil.toJsonString(devInfo));
        }

        // 如果判定指令完成（关门到位+锁住），标记事件完成
        if (eventFinish) {
            deviceService.eventFinish(devInfo.getEventId());
        }

        // 异常开门：写入事件记录（用于追溯）
        if (devInfo.getUnexpectedOpen() == 1) {
            deviceService.addEvent(devInfo.getImei(), JsonUtil.toJsonString(devInfo));
        }

        // ----------------------- 7）更新设备最新状态缓存 -----------------------
        // 将 devInfo 写入缓存，供其它业务快速获取最新设备状态（减少 DB 访问）
        DeviceInfoCache.createInfo(devInfo);

        // ----------------------- 8）回复终端通用应答（CommonResp） -----------------------
        // 终端上报后，平台通常需要回复通用应答，表示“已收到并处理”
        // 这里使用下行流水号：getSerialNumber(channel)（每条连接递增）
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));

        log.info("WetWipeInfoReport resp success:{} devInfo:{}", resp.getResult(), JsonUtil.toJsonString(devInfo));

        // 返回 TransReportDTO：
        // - 第一个 true：表示处理成功
        // - 第二个 true：表示需要回包
        // - 第三个 resp：实际要回复的应答包
        return new TransReportDTO(true, true, resp);
    }

    /**
     * 将 ByteBuf 转成具体的 ReportMsg 子类
     * - 工厂根据 messageType 选中本策略后，会调用该方法构造具体报文对象
     * - WetWipeInfoReportResp 内部会在 parse() 时解析 payload 成字段
     */
    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new WetWipeInfoReportResp(byteBuf);
    }
}

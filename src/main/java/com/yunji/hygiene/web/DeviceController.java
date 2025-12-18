package com.yunji.hygiene.web;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.config.HMACAuth;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.po.ContainerCellPO;
import com.yunji.hygiene.response.Response;
import com.yunji.hygiene.response.ResponseHelper;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * 设备相关接口（湿纸巾设备/货柜）控制器
 *
 * 主要职责：
 * 1) 提供设备指令下发入口（command/reset/upgrade 等）
 * 2) 提供设备在线状态检测（status）
 * 3) 提供设备强制置为在线/离线（online/offline，用于后台或运维校正状态）
 * 4) 提供设备连接列表查看（statusList）
 * 5) 提供设备“休眠标记”的批量设置/删除（sleepList/deleteSleep，写 Redis）
 *
 * 注意点：
 * - 部分接口使用 @HMACAuth 进行鉴权（通常用于设备或内部系统调用，防止未授权请求）
 * - 在线状态不仅看 ChannelManager.online，还会通过 ping 获取 ACK 作为最终判定
 * - sleepList/deleteSleep 支持逗号分隔的 imei 批量操作
 */
@RestController
@RequestMapping("/device/hygiene") // 统一的路由前缀
@Slf4j // Lombok：自动注入 log 变量（org.slf4j.Logger）
public class DeviceController {

    /**
     * 设备指令调用服务（通常包含：指令封装、通道发送、等待 ACK、超时处理等）
     *
     * 典型方法：
     * - reset(imei)：设备重启/复位
     * - command(cmd)：通用指令下发
     * - ping(imei, needAck)：探活并等待 ACK
     * - cacheFileUpgrade(cmd)：固件/资源包升级（通常先缓存升级信息，再触发设备下载/执行）
     */
    @Resource
    private DeviceCallService deviceCallService;

    /**
     * 设备业务服务（通常包含：设备/货柜数据查询、状态落库、缓存同步等）
     *
     * 典型方法：
     * - getCell(id)：查询某个货道/格子信息（示例）
     * - cabinetOnline/cabinetOffline：设备上下线处理（可能更新 DB、缓存、触发事件等）
     */
    @Resource
    private DeviceService deviceService;

    /**
     * 测试接口：读取某个格子（货道）信息并打印
     *
     * URL: GET /device/hygiene/test
     * 返回：统一响应成功
     *
     * 说明：
     * - 这里的 3459L 是硬编码测试数据，仅用于开发/联调阶段验证 service 是否正常
     * - System.out.println 仅建议在本地开发使用，线上建议改成 log（此处不改逻辑，只做说明）
     */
    @GetMapping(value = "/test")
    public Response<?> test() {
        // 从业务层查询某个格子信息（例如某个货柜的某个格子/货道）
        ContainerCellPO cell = deviceService.getCell(3459L);

        // 打印到控制台（调试用）
        System.out.println(cell);

        // 返回统一成功响应（无数据）
        return ResponseHelper.success();
    }

    /**
     * 设备复位/重启接口
     *
     * URL: GET /device/hygiene/reset/{imei}
     * PathVariable: imei（设备唯一标识）
     *
     * 返回：
     * - ResponseHelper.success(deviceCallService.reset(imei))：将 reset 的执行结果作为 data 返回
     *
     * 典型用途：
     * - 运维/后台手动触发设备重启
     * - 工厂测试
     */
    @GetMapping(value = "/reset/{imei}")
    public Response<?> reset(@PathVariable String imei) {
        // 下发 reset 指令并返回执行结果
        return ResponseHelper.success(deviceCallService.reset(imei));
    }

    /**
     * HMAC 鉴权测试接口
     *
     * URL: POST /device/hygiene/testHmacAuth
     * Header/签名：由 @HMACAuth 处理（通常会校验时间戳、签名、body 摘要等）
     * Body: EnterCommandDTO（校验 @Valid）
     *
     * 说明：
     * - 主要用于验证 @HMACAuth 注解是否生效、签名/验签链路是否正确
     */
    @HMACAuth
    @PostMapping(value = "/testHmacAuth")
    public Response<?> testHmacAuth(@RequestBody @Valid EnterCommandDTO cmd) {
        // 简单打印 imei（调试用）
        System.out.println(cmd.getImei());
        return ResponseHelper.success();
    }

    /**
     * 通用指令下发接口（对设备下达“进入/执行某动作”等命令）
     *
     * URL: POST /device/hygiene/command
     * 鉴权：@HMACAuth
     * Body: EnterCommandDTO（必填字段由 DTO + @Valid 约束）
     *
     * 执行流程（当前代码实际逻辑）：
     * 1) 直接调用 deviceCallService.command(cmd) 下发指令
     * 2) 若返回 true：认为指令下达成功 -> success
     * 3) 否则：failure("指令下达失败:imei")
     *
     * 注意：
     * - 代码中原本存在 ping 预检查逻辑（已注释掉）：即先 ping 在线再下发
     * - 当前实现是“直接下发”，失败与否由 deviceCallService.command 返回
     */
    @HMACAuth
    @PostMapping(value = "/command")
    public Response<String> command(@RequestBody @Valid EnterCommandDTO cmd) {
        // 原逻辑（已注释）：先 ping 探活再下发
        // boolean ping = deviceCallService.ping(cmd.getImei(), false);
        // if (ping) {

        // 直接下发指令：由底层负责写入 channel、等待回执/超时等
        boolean command = deviceCallService.command(cmd);

        // 若下发成功：返回统一成功
        if (command) {
            return ResponseHelper.success();
        }

        // 下发失败：返回统一失败，并携带 imei 便于排查
        // }

        return ResponseHelper.failure("指令下达失败:" + cmd.getImei());
    }

    /**
     * 设备升级接口（固件/资源包升级）
     *
     * URL: POST /device/hygiene/upgrade
     * 鉴权：@HMACAuth
     * Body: UpgradeCommandDTO（校验 @Valid）
     *
     * 执行流程：
     * 1) 记录升级参数日志（便于审计与排障）
     * 2) 调用 deviceCallService.cacheFileUpgrade(cmd)
     *    - 通常会：缓存升级任务 -> 触发设备拉取/执行 -> 等待 ACK/状态回传
     * 3) 返回 success/failure
     */
    @HMACAuth
    @PostMapping(value = "/upgrade")
    public Response<String> upgrade(@RequestBody @Valid UpgradeCommandDTO cmd) {
        // 记录请求入参（升级指令很关键，务必可追踪）
        log.info("DeviceController upgrade :{}", JsonUtil.toJsonString(cmd));

        // 执行“缓存文件升级”命令（名字暗示：可能先写缓存/Redis，再通知设备）
        boolean command = deviceCallService.cacheFileUpgrade(cmd);

        // 成功则返回统一 success
        if (command) {
            return ResponseHelper.success();
        }

        // 失败则返回统一 failure，并带 imei
        return ResponseHelper.failure("升级失败:" + cmd.getImei());
    }

    /*
     * 设备处理接口（示例/预留）
     * - 当前注释掉，可能用于“设备异常处理/工单处理/远程处理某状态”等
     */
//    @PostMapping(value = "/handle")
//    public Response<String> handle(@RequestBody HygieneHandleDTO handle) {
//        boolean command = deviceCallService.handle(handle);
//        if (command)
//            return ResponseHelper.success();
//        return ResponseHelper.failure("处理失败:" + handle.getImei());
//    }

    /**
     * 设备在线状态检测（强校验版：Channel 在线 + ping ACK）
     *
     * URL: GET /device/hygiene/status/{imei}
     * 返回：boolean（true=收到 ping ACK；false=未收到）
     *
     * 当前判定逻辑：
     * 1) 先用 ChannelManager.online(imei) 判断 Netty 通道是否存在/活跃
     *    - 这一步通常代表“服务端是否还持有该设备连接”
     * 2) 再调用 deviceCallService.ping(imei, true) 发送 ping 并等待 ACK
     *    - ACK 成功才返回 true
     *
     * 日志：
     * - 记录 channel 在线结果
     * - 记录 ping ack 结果
     *
     * 备注：
     * - 代码里有 updateStatus 的注释逻辑：可能用于离线落库/在线落库，这里先保留注释
     */
    @GetMapping(value = "/status/{imei}")
    public boolean status(@PathVariable String imei) {
        // 1) 通过 ChannelManager 判断当前服务端是否认为该设备在线（channel 存在且活跃）
        boolean online = ChannelManager.online(imei);
        log.info("hygiene device online status imei:{},online:{}", imei, online);

        // 2) 通过 ping 实际探活（并等待 ACK），ACK 才是更可靠的在线判定
        // 注意：这里无论 online true/false 都会 ping（因为注释掉了 if (online)）
        boolean ack = deviceCallService.ping(imei, true);
        log.info("hygiene device ping ack imei:{},online:{}", imei, ack);

        // 3) 返回探活结果
        return ack;
    }

    /**
     * 强制标记设备在线（通常用于设备回调、内部系统通知、人工修正）
     *
     * URL: GET /device/hygiene/online/{imei}
     * 鉴权：@HMACAuth
     *
     * 执行：
     * - deviceService.cabinetOnline(imei, true)
     *   第二个参数 true 可能表示“强制/来自设备端/来自内部调用”等语义（按你们项目定义）
     *
     * 返回：boolean true（简单响应）
     */
    @HMACAuth
    @GetMapping(value = "/online/{imei}")
    public boolean online(@PathVariable String imei) {
        // 将设备设置为在线（可能会：更新 DB 状态、缓存、触发事件、记录心跳时间等）
        deviceService.cabinetOnline(imei, true);
        return true;
    }

    /**
     * 强制标记设备离线（通常用于设备断连回调、内部系统通知、人工修正）
     *
     * URL: GET /device/hygiene/offline/{imei}
     * 鉴权：@HMACAuth
     *
     * 执行：
     * - deviceService.cabinetOffline(imei, true)
     */
    @HMACAuth
    @GetMapping(value = "/offline/{imei}")
    public boolean offline(@PathVariable String imei) {
        // 将设备设置为离线（可能会：更新 DB 状态、缓存、触发告警/通知等）
        deviceService.cabinetOffline(imei, true);
        return true;
    }

    /**
     * 获取当前所有在线设备的 Channel 列表（调试/运维查看）
     *
     * URL: GET /device/hygiene/statusList
     * 返回：Map<String, Channel>
     * - key：通常是 imei（或你们内部的连接标识）
     * - value：Netty Channel（连接对象）
     *
     * 备注：
     * - 此接口未启用 @HMACAuth（注释掉了），如果上线环境建议加鉴权或限制访问
     * - 直接暴露 Channel 结构给外部可能不安全，通常只用于内部排障
     */
    //@HMACAuth
    @GetMapping(value = "/statusList")
    public Map<String, Channel> statusList() {
        // 直接返回 ChannelManager 中维护的连接映射
        return ChannelManager.getChannelMap();
    }

    /**
     * 批量设置设备“休眠标记”
     *
     * URL: GET /device/hygiene/sleepList/{imei}
     * PathVariable：imei
     * - 支持单个 imei：例如 1234567890
     * - 支持多个 imei 逗号分隔：例如 111,222,333
     *
     * 行为：
     * - 对每个 imei 写入 Redis：
     *   key = DeviceCacheCode.DEVICE_SLEEP + imei
     *   value = new Date()（写入时间，便于排查/追踪）
     *   ttl = 2000 HOURS（注意：这里是“小时”，约 83 天左右）
     *
     * 说明：
     * - HOURS 来自静态导入 TimeUnit.HOURS
     * - 2000 HOURS 是硬编码 TTL，通常表示“长时间休眠/长期忽略某些逻辑”
     * - 实际业务上可能用于：避免频繁 ping、避免下发指令、跳过某些巡检等（按你们逻辑）
     */
    @GetMapping(value = "/sleepList/{imei}")
    public boolean sleepList(@PathVariable String imei) {
        // 1) 按逗号拆分，支持批量操作
        String[] split = imei.split(",");

        // 2) 逐个写入 Redis 休眠标记
        for (String s : split) {
            // set(key, value, timeout, unit)
            SystemUtil.redisCache.set(
                    DeviceCacheCode.DEVICE_SLEEP + s, // Redis key：休眠标记前缀 + imei
                    new Date(),                       // Redis value：当前时间（调试/审计用）
                    2000,                             // TTL：2000
                    HOURS                             // TTL 单位：小时
            );
            log.info("{}休眠成功", s);
        }
        return true;
    }

    /**
     * 批量删除设备“休眠标记”
     *
     * URL: GET /device/hygiene/deleteSleep/{imei}
     * PathVariable：imei（支持逗号分隔）
     *
     * 行为：
     * - 对每个 imei 执行 Redis delete：
     *   key = DeviceCacheCode.DEVICE_SLEEP + imei
     *
     * 典型用途：
     * - 恢复设备正常巡检/正常指令下发
     * - 运维解除休眠状态
     */
    @GetMapping(value = "/deleteSleep/{imei}")
    public boolean deleteSleep(@PathVariable String imei) {
        // 1) 支持批量
        String[] split = imei.split(",");

        // 2) 逐个删除 Redis key
        for (String s : split) {
            SystemUtil.redisCache.delete(DeviceCacheCode.DEVICE_SLEEP + s);
            log.info("{}删除休眠", s);
        }
        return true;
    }
}

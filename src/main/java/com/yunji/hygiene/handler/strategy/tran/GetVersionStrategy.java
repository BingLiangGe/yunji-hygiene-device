package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * GetVersionStrategy：查询设备版本指令策略
 *
 * 作用：
 * - 构造一个“空包体”的透传消息（EmptyTransMsg）
 * - messageType 设置为 GET_VERSION
 * - 设备端收到后通常会回传“当前固件版本/软件版本”等信息（通过上报/应答报文返回）
 *
 * 典型使用场景：
 * - 后台页面点击“获取设备版本”
 * - 升级前校验：先获取设备当前版本，再决定是否需要升级
 * - 运维排查：确认设备固件是否一致
 *
 * 注意：
 * - 该策略仅负责构造透传消息体
 * - JT808 头、流水号、校验码、转义、分隔符等由后续统一封装与编码器完成
 */
public class GetVersionStrategy implements ITransMsgStrategy {

    /**
     * 构造“获取版本”透传消息
     *
     * @param cmd 命令入参（该指令无需额外参数，因此这里不使用 cmd；保留是为了接口统一）
     * @return EmptyTransMsg（继承 TransMsg），后续会被封装成 JT808 8900 下发报文
     */
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {

        // 1）GET_VERSION 无额外参数，使用空透传消息体
        EmptyTransMsg getVersionMsg = new EmptyTransMsg();

        // 2）eventId = -1：表示不关联业务事件（无需事件跟踪/回写）
        getVersionMsg.setEventId(-1);

        // 3）设置透传消息类型：GET_VERSION（设备端据此返回版本信息）
        getVersionMsg.setMessageType(TransEnum.GET_VERSION.getIssueType());

        // 4）返回消息体
        return getVersionMsg;
    }
}

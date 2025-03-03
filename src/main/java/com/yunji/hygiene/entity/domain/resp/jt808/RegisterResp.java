package com.yunji.hygiene.entity.domain.resp.jt808;

import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:注册响应包
 * @Version: 1.0
 */
@Data
public class RegisterResp extends DataPacket {

    public static final byte SUCCESS = 0;//成功
    public static final byte VEHICLE_ALREADY_REGISTER = 1;//车辆已被注册
    public static final byte NOT_IN_DB = 2;//数据库无该车辆
    public static final byte TERMINAL_ALREADY_REGISTER = 3;//终端已被注册

    private short replyFlowId; //应答流水号 2字节
    private byte result;    //结果 1字节
    private String authCode; //鉴权码

    public RegisterResp() {
        this.getHeader().setMsgId(JT808Const.SERVER_RESP_REGISTER);
    }

    @Override
    public ByteBuf toByteBufMsg() {
        ByteBuf bb = super.toByteBufMsg();
        bb.writeShort(replyFlowId);
        bb.writeByte(result);
        if (result == SUCCESS && StringUtils.isNotBlank(authCode)) {//成功才写入鉴权码
            bb.writeBytes(authCode.getBytes(JT808Const.DEFAULT_CHARSET));
        }
        return bb;
    }

    public static RegisterResp success(DataPacket msg, short flowId) {
        RegisterResp resp = new RegisterResp();
        resp.getHeader().setImei(msg.getHeader().getImei());
        resp.getHeader().setFlowId(flowId);
        resp.setReplyFlowId(msg.getHeader().getFlowId());
        resp.setResult(SUCCESS);
        resp.setAuthCode("SUCCESS");
        return resp;
    }
}

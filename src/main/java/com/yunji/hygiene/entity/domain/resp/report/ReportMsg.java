package com.yunji.hygiene.entity.domain.resp.report;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Project: yunji-cup
 * @Package: net.virtuemed.jt808.vo.req
 * @Title: ReportMsg
 * <p>
 * History:
 * Date                     Version     Author          Summary
 * ============================================================
 * 2024-10-17 17:40:04      V1.0        huaaotian       新建类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class ReportMsg extends DataPacket {

    private byte messageType; // 透传消息类型 1字节
    private int messageLength; // 消息总长度 4字节
    private short packageLength; // 包信息长度 2字节

    public ReportMsg(ByteBuf payload) {
        super(payload);
    }

    @Override
    protected void parseBody() {
        ByteBuf bb = this.payload;
        this.setMessageType(bb.readByte());
        this.setMessageLength(bb.readInt());
        this.setPackageLength(bb.readShort());
    }
}
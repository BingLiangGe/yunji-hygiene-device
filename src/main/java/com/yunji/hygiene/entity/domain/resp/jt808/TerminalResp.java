package com.yunji.hygiene.entity.domain.resp.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : peter-zhu
 * @date : 2025/2/28 16:11
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TerminalResp extends DataPacket {
    private short replyFlowId; //应答流水号 2字节
    private short replyId; //应答 ID  2字节
    private byte result;

    public TerminalResp(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void parseBody() {
        super.parseBody();
        this.setReplyFlowId(super.payload.readShort());
        this.setReplyId(super.payload.readShort());
        this.setResult(super.payload.readByte());
    }
}

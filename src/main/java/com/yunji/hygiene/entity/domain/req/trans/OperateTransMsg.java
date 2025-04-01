package com.yunji.hygiene.entity.domain.req.trans;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

@EqualsAndHashCode(callSuper = true)
@Data
public class OperateTransMsg extends TransMsg {

    private byte openNum;
    private byte action;

    public OperateTransMsg() {
    }

    @Override
    public byte[] getContentBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(openNum);
        buffer.put(action);
        return buffer.array();
    }
}

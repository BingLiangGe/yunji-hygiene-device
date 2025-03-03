package com.yunji.hygiene.entity.domain.req.trans;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.handler.strategy.report.ITranReportMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

/**
 * @author : peter-zhu
 * @date : 2025/2/14 18:14
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class OtaTransMsg extends TransMsg {


    private byte otaId;
    private byte[] chunk;

    public byte[] getContentBytes() {
        // 堆内存 可以自动回收 不用担心内存泄漏
        ByteBuffer buffer = ByteBuffer.allocate(1 + ITranReportMsg.PACKET_SIZE);
        buffer.put(otaId);
        buffer.put(chunk);
        return buffer.array();
    }
}

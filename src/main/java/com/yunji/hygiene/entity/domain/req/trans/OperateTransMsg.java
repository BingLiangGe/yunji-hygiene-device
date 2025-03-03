package com.yunji.hygiene.entity.domain.req.trans;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.nio.ByteBuffer;
/**
 * @author : peter-zhu
 * @date : 2025/1/13 10:05
 * @description : 操作不同类型的开柜关柜指令
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class OperateTransMsg extends TransMsg {

    private byte openNum;
    private byte action;

    public OperateTransMsg() {
    }



    public byte[] getContentBytes() {
        // 堆内存 可以自动回收 不用担心内存泄漏
        ByteBuffer buffer = ByteBuffer.allocate(2 + 2);
        buffer.put(openNum);
        buffer.put(action);
        return buffer.array();
    }
}

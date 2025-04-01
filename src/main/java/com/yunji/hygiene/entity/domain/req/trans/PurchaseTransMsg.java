package com.yunji.hygiene.entity.domain.req.trans;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.dto.ShoppingDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2025/1/13 10:05
 * @description : 操作不同类型的开柜关柜指令
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class PurchaseTransMsg extends TransMsg {

    List<ShoppingDTO> pairList;

    public PurchaseTransMsg() {
    }

    public byte[] getContentBytes() {
        // 堆内存 可以自动回收 不用担心内存泄漏
        ByteBuffer buffer = ByteBuffer.allocate(pairList.size() * 2);
        for (ShoppingDTO dto : pairList) {
            buffer.put(dto.getOrdinal());
            buffer.put(dto.getNums());
        }
        return buffer.array();
    }
}

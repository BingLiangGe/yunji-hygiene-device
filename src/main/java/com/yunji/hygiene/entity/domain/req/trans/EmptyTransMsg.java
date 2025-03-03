package com.yunji.hygiene.entity.domain.req.trans;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 14:26
 * @description : TODO
 **/
public class EmptyTransMsg extends TransMsg {
    @Override
    public byte[] getContentBytes() {
        return new byte[0];
    }
}

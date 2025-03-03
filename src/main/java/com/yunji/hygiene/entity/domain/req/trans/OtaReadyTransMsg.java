package com.yunji.hygiene.entity.domain.req.trans;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.util.AsciiUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : peter-zhu
 * @date : 2025/2/19 17:12
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class OtaReadyTransMsg extends TransMsg {
    private String versionPrefix;

    @Override
    public byte[] getContentBytes() {
        return AsciiUtil.toByte(versionPrefix);
    }
}

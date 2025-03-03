package com.yunji.hygiene.entity.domain.resp.report;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : peter-zhu
 * @date : 2025/2/12 15:49
 * @description : TODO
 **/
@Setter
@Getter
public class UpgradeResp extends ReportMsg {

    private byte otaId;
    private String result;

    public UpgradeResp(ByteBuf payload) {
        super(payload);
    }

    @Override
    public void parseBody() {
        super.parseBody();
        ByteBuf bb = this.payload;
        this.setOtaId(bb.readByte());
        this.setResult(super.readAsciiByBytes(super.getPackageLength() - 1));
    }

}
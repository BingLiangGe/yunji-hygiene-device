package com.yunji.hygiene.entity.domain.resp.report;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : peter-zhu
 * @date : 2025/2/19 16:26
 * @description : TODO
 **/
@Setter
@Getter
public class GetVersionResp extends ReportMsg {

    private String version;

    public GetVersionResp(ByteBuf payload) {
        super(payload);
    }

    @Override
    public void parseBody() {
        super.parseBody();
        this.setVersion(super.readAsciiByBytes(super.getPackageLength()));
    }
}

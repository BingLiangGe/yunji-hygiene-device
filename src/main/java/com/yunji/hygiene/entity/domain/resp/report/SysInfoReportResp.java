package com.yunji.hygiene.entity.domain.resp.report;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : peter-zhu
 * @date : 2025/2/15 13:44
 * @description : TODO
 **/
@Setter
@Getter
public class SysInfoReportResp extends ReportMsg{
    private int eventId; // 事件ID 4字节
    private byte sleepMode;
    private byte outLimitStatus; // 出仓 关闭为0
    private byte inLimitStatus; // 进仓 关闭为0
    private byte distanceMsb;  //红外距离高8位
    private byte distanceLsb;  //红外距离低8位
    private byte battleLevel;
    private byte lockStatus;
    private byte tissueStatus;

    public SysInfoReportResp(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void parseBody() {
        super.parseBody();
        ByteBuf bb = this.payload;
        this.setEventId(bb.readInt());
        this.setSleepMode(bb.readByte());
        this.setOutLimitStatus(bb.readByte());
        this.setInLimitStatus(bb.readByte());
        this.setDistanceMsb(bb.readByte());
        this.setDistanceLsb(bb.readByte());
        this.setBattleLevel(bb.readByte());
        this.setLockStatus(bb.readByte());
        this.setTissueStatus(bb.readByte());
    }
}

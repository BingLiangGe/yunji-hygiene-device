package com.yunji.hygiene.entity.domain.resp.report;


import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class HygieneInfoReportResp extends ReportMsg {

    private int eventId; // 事件ID 4字节
    private byte lockStatus;
    private byte rssi;
    private List<Byte> limitList;
    private List<Short> distanceList;
    private List<Byte> motorStatusList;

    public HygieneInfoReportResp(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    protected void parseBody() {
        super.parseBody();
        ByteBuf bb = this.payload;
        this.setEventId(bb.readInt());
        this.setLockStatus(bb.readByte());
        this.setRssi(bb.readByte());
        List<Byte> limitList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            limitList.add(bb.readByte());
        }
        this.setLimitList(limitList);
        List<Short> distanceList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            distanceList.add(bb.readShort());
        }
        this.setDistanceList(distanceList);
        List<Byte> motorStatusList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            motorStatusList.add(bb.readByte());
        }
        this.setMotorStatusList(motorStatusList);
    }

    @Override
    public String toString() {
        return "HygieneInfoReportResp{" +
                "eventId=" + eventId +
                ", lockStatus=" + lockStatus +
                ", rssi=" + rssi +
                ", limitList=" + limitList +
                ", distanceList=" + distanceList +
                '}';
    }
}

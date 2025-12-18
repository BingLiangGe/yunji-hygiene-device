package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.util.BCD;
import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * LocationMsg：位置信息上报（上行 0x0200）
 *
 * 消息体字段（按 JT808 常见格式顺序解析）：
 * - alarm        4字节：告警标志位
 * - statusField  4字节：状态位
 * - latitude     4字节：纬度（单位 1e-6 度，解析后 = raw / 1_000_000）
 * - longitude    4字节：经度（单位 1e-6 度，解析后 = raw / 1_000_000）
 * - elevation    2字节：海拔（米）
 * - speed        2字节：速度（通常 0.1km/h，具体以终端实现为准）
 * - direction    2字节：方向（0~359）
 * - time         6字节：BCD 时间（YYMMDDhhmmss）
 *
 * 说明：
 * - DataPacket 已完成 header 解析，payload 指向消息体（body）
 * - parseBody() 只负责按协议顺序读取 payload
 */
@Data
public class LocationMsg extends DataPacket {

    public LocationMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }

    private int alarm;          // 告警标志位（4字节）
    private int statusField;    // 状态位（4字节）
    private float latitude;     // 纬度（4字节，raw/1e6）
    private float longitude;    // 经度（4字节，raw/1e6）
    private short elevation;    // 海拔（2字节）
    private short speed;        // 速度（2字节，常见单位 0.1km/h）
    private short direction;    // 方向（2字节，0~359）
    private String time;        // 时间（6字节 BCD：YYMMDDhhmmss）

    @Override
    public void parseBody() {
        ByteBuf bb = this.payload;

        this.setAlarm(bb.readInt());
        this.setStatusField(bb.readInt());

        // 经纬度字段为“无符号整型”，单位为 1e-6 度
        this.setLatitude(bb.readUnsignedInt() * 1.0F / 1_000_000);
        this.setLongitude(bb.readUnsignedInt() * 1.0F / 1_000_000);

        this.setElevation(bb.readShort());
        this.setSpeed(bb.readShort());
        this.setDirection(bb.readShort());

        // BCD 时间：YYMMDDhhmmss
        this.setTime(BCD.toBcdTimeString(readBytes(6)));
    }
}

package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * RegisterMsg：终端注册消息（上行 0x0100）
 *
 * 消息体字段（按 JT808 注册包常见格式顺序解析）：
 * - provinceId         2字节：省域 ID
 * - cityId             2字节：市/县 ID
 * - manufacturerId     5字节：制造商 ID（定长字符串，GBK/ASCII，可能右侧补 0x00）
 * - terminalType       8字节：终端型号（定长字符串）
 * - terminalId         7字节：终端 ID（定长字符串）
 * - licensePlateColor  1字节：车牌颜色
 * - licensePlate       N字节：车牌号（剩余全部字节，变长字符串）
 *
 * 说明：
 * - DataPacket 已解析 header，并把消息体放到 payload（ByteBuf）
 * - parseBody() 只负责按协议顺序读取 payload
 */
@Data
public class RegisterMsg extends DataPacket {

    private short provinceId;         // 省域ID（2字节）
    private short cityId;             // 市县ID（2字节）
    private String manufacturerId;    // 制造商ID（5字节定长）
    private String terminalType;      // 终端型号（8字节定长）
    private String terminalId;        // 终端ID（7字节定长）
    private byte licensePlateColor;   // 车牌颜色（1字节）
    private String licensePlate;      // 车牌号（剩余字节，变长）

    public RegisterMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void parseBody() {
        ByteBuf bb = this.payload;

        this.setProvinceId(bb.readShort());
        this.setCityId(bb.readShort());

        // 定长字符串：注意终端可能用 0x00 补齐，readString 内建议 trim
        this.setManufacturerId(readString(5));
        this.setTerminalType(readString(8));
        this.setTerminalId(readString(7));

        this.setLicensePlateColor(bb.readByte());

        // 车牌号为变长字段：读取 payload 剩余全部字节
        this.setLicensePlate(readString(bb.readableBytes()));
    }
}

package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:终端注册包
 * @Version: 1.0
 */
@Data
public class RegisterMsg extends DataPacket {

    private short provinceId;//省域ID 2字节
    private short cityId;//市县ID 2字节
    private String manufacturerId;//制造商ID 5字节
    private String terminalType;//终端型号 8字节
    private String terminalId;//终端ID 7字节
    private byte licensePlateColor;//车牌颜色 1字节
    private String licensePlate;//车牌号 剩余字节

    public RegisterMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void parseBody() {
        ByteBuf bb = this.payload;
        this.setProvinceId(bb.readShort());
        this.setCityId(bb.readShort());
        this.setManufacturerId(readString(5));
        this.setTerminalType(readString(8));
        this.setTerminalId(readString(7));
        this.setLicensePlateColor(bb.readByte());
        this.setLicensePlate(readString(bb.readableBytes()));
    }
}

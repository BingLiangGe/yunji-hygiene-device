package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author : peter-zhu
 * @date : 2025/1/14 11:28
 * @description : 透传消息体  8900指令
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class TransMsg extends DataPacket implements Serializable {
    private static final long serialVersionUID = -2035248813458629692L;

    private byte messageType; // 透传消息类型 1字节
    private int messageLength; // 消息总长度 4字节
    private short packageLength; // 包信息长度 2字节
    private int eventId;

    public abstract byte[] getContentBytes();

    //FIXME 暂时不分包，不分包的话  messageLength==packageLength 是一样的
    public int getMessageLength() {
        if (messageLength == 0) {
            if (eventId != -1)
                return 4 + getContentBytes().length;
            return getContentBytes().length;
        }
        return messageLength;
    }

    public short getPackageLength() {
        if (packageLength == 0) {
            if (eventId != -1)
                return (short) (4 + getContentBytes().length);
            return (short) getContentBytes().length;
        }
        return packageLength;
    }

}
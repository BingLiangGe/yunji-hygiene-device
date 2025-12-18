package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * TransMsg：0x8900（透传/下发）消息体抽象基类
 *
 * 设计目标：
 * - 统一描述 0x8900 指令的“透传包体结构”
 * - 让不同 messageType 的具体指令只关心自己的 content（getContentBytes）
 *
 * 常见包体结构（你们自定义透传协议）：
 * [messageType:1][messageLength:4][packageLength:2][(eventId:4)?][content:N]
 *
 * 字段说明：
 * - messageType：透传子类型（1字节），用于设备端/平台端二次路由
 * - messageLength：消息总长度（通常指 eventId+content 的长度或全体长度，需与设备端约定一致）
 * - packageLength：当前包长度（支持未来分包；目前不分包时通常等于 messageLength）
 * - eventId：事件/任务ID（用于将下发命令与回执/ACK 关联；-1 表示不携带）
 *
 * 长度计算（目前不分包）：
 * - 若 eventId != -1：长度 = 4(eventId) + content.length
 * - 若 eventId == -1：长度 = content.length
 *
 * 注意：
 * - messageLength/packageLength 默认为 0 时，走动态计算；非 0 表示外部已显式赋值（例如后续分包场景）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class TransMsg extends DataPacket implements Serializable {
    private static final long serialVersionUID = -2035248813458629692L;

    /** 透传子类型：决定具体指令含义（1字节） */
    private byte messageType;

    /** 消息总长度（4字节）：默认 0 表示运行时按 content/eventId 自动计算 */
    private int messageLength;

    /** 当前包长度（2字节）：默认 0 表示运行时按 content/eventId 自动计算 */
    private short packageLength;

    /** 事件/任务ID：用于关联下发与回执；-1 表示不携带 eventId 字段 */
    private int eventId;

    /** 子类实现：返回具体透传内容（不含 eventId 与长度字段） */
    public abstract byte[] getContentBytes();

    // FIXME：暂时不分包；因此 messageLength == packageLength（逻辑上相等）
    public int getMessageLength() {
        if (messageLength == 0) {
            // eventId != -1 => 透传内容前额外带 4字节 eventId
            if (eventId != -1) return 4 + getContentBytes().length;
            return getContentBytes().length;
        }
        return messageLength;
    }

    public short getPackageLength() {
        if (packageLength == 0) {
            if (eventId != -1) return (short) (4 + getContentBytes().length);
            return (short) getContentBytes().length;
        }
        return packageLength;
    }
}

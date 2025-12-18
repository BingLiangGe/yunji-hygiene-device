package com.yunji.hygiene.entity.domain;

import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.util.AsciiUtil;
import com.yunji.hygiene.util.ImeiUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;

/**
 * DataPacket：JT808 报文抽象基类（统一解析 Header + Body）
 *
 * 设计目标：
 * - Decoder 解析出不同消息类型（Register/Auth/Heart/Location/Trans...）时，都继承 DataPacket
 * - DataPacket 负责解析通用 Header（msgId、msgBodyProps、imei、flowId、分包标记等）
 * - 子类只需要重写 parseBody() 解析消息体（payload 的剩余部分）
 *
 * 关键点：
 * 1) parse() 会在 finally 里释放 payload（safeRelease）
 *    - 表示：parse 完成后，这个 ByteBuf 不应再被读取（避免内存泄漏）
 * 2) msgBodyProps 是“消息体属性”位段：
 *    - 低10位：消息体长度
 *    - bit13：是否分包
 *    - bits10~12：加密类型
 * 3) toByteBufMsg() 用于“下行/应答编码”：
 *    - 先写入 4 字节占位（msgId + msgBodyProps），由 JT808Encoder 统一回填
 *    - 再写入 imei(6) + flowId(2) + (分包字段 TODO)
 */
@Data
public class DataPacket {

    /** 报文头（通用字段） */
    protected Header header = new Header();

    /** 报文体（ByteBuf），解析时从这里顺序读取 */
    protected ByteBuf payload;

    public DataPacket() {}

    public DataPacket(ByteBuf payload) {
        this.payload = payload;
    }

    /**
     * 解析完整报文：先解析头部，再校验 bodyLen，再解析 body
     * 注意：finally 会释放 payload，parse 完成后不要再读 payload
     */
    public void parse() {
        try {
            this.parseHead();

            // 校验“header 声明的 bodyLen”和“实际剩余字节数”一致，防止错包/截断
            if (this.header.getMsgBodyLength() != this.payload.readableBytes()) {
                throw new RuntimeException("包体长度有误");
            }

            this.parseBody();
        } finally {
            ReferenceCountUtil.safeRelease(this.payload);
        }
    }

    /** 解析 JT808 Header：msgId(2) + msgBodyProps(2) + imei(6) + flowId(2) + (分包字段 TODO) */
    protected void parseHead() {
        header.setMsgId(payload.readShort());
        header.setMsgBodyProps(payload.readShort());
        header.setImei(ImeiUtil.byteToImei(readBytes(6)));
        header.setFlowId(payload.readShort());

        if (header.hasSubPackage()) {
            // TODO：分包信息（通常包含 totalPkg、pkgSeq 等）
            payload.readInt();
        }
    }

    /** 子类重写：解析消息体（payload 剩余部分） */
    protected void parseBody() {}

    /**
     * 构建“待发送”的 ByteBuf（header 基础字段）
     * - 先占位 4 字节给 msgId + msgBodyProps，JT808Encoder 会回填
     * - 写 imei(6) + flowId(2)
     */
    public ByteBuf toByteBufMsg() {
        ByteBuf bb = ByteBufAllocator.DEFAULT.heapBuffer(); // 在 JT808Encoder.escape() 中释放
        bb.writeInt(0); // 占位：msgId(2) + msgBodyProps(2)，后续 Encoder 覆盖写入
        bb.writeBytes(ImeiUtil.imeiToByte(this.header.getImei()));
        bb.writeShort(this.header.getFlowId());
        // TODO：分包字段
        return bb;
    }

    /** 从 payload 读取固定长度字节数组（会推进 readerIndex） */
    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        this.payload.readBytes(bytes);
        return bytes;
    }

    /** 读取固定长度字符串（按 JT808 默认字符集 GBK 解码） */
    public String readString(int length) {
        return new String(readBytes(length), JT808Const.DEFAULT_CHARSET);
    }

    /** 读取 ASCII 字符串（常用于设备厂商ID/型号等固定字段） */
    public String readAsciiByBytes(int len) {
        byte[] bytes = new byte[len];
        this.payload.readBytes(bytes);
        return AsciiUtil.toStr(bytes);
    }

    /** JT808 Header（通用头字段） */
    @Data
    public static class Header {
        private short msgId;        // 消息ID（2字节）
        private short msgBodyProps; // 消息体属性（2字节，包含长度/加密/分包标记等）
        private String imei;        // 终端标识（你们这里用 imei，6字节编码）
        private short flowId;       // 流水号（2字节）

        /** 消息体长度：取 msgBodyProps 低10位 */
        public short getMsgBodyLength() {
            return (short) (msgBodyProps & 0x3ff);
        }

        /** 加密类型：bits10~12（3bit） */
        public byte getEncryptionType() {
            return (byte) ((msgBodyProps & 0x1c00) >> 10);
        }

        /** 是否分包：bit13 */
        public boolean hasSubPackage() {
            return ((msgBodyProps & 0x2000) >> 13) == 1;
        }
    }
}

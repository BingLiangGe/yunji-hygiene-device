package com.yunji.hygiene.constant;

import java.nio.charset.Charset;

/**
 * JT808Const：JT/T 808 协议常量定义（消息ID、分隔符、默认字符集等）
 *
 * 主要用于：
 * - Decoder：根据 msgId 分发到不同 DataPacket/Handler
 * - Encoder：拼装 header、写入 msgId、转义并加包头/包尾分隔符
 *
 * 关键点：
 * - PKG_DELIMITER=0x7E：标识一帧报文的开始/结束（需要配合转义规则）
 * - msgId（short）：JT808 消息 ID（2字节无符号，Java 用 short 承载）
 */
public class JT808Const {

    /** JT808 默认字符集：通常使用 GBK（用于终端/平台字符串字段编码） */
    public static final Charset DEFAULT_CHARSET = Charset.forName("GBK");

    /**
     * 帧分隔符 0x7E
     * - 一帧报文格式通常：0x7E + (转义后的数据) + 0x7E
     * - 数据区内出现 0x7E/0x7D 需要转义（与你的 Encoder/Decoder 的 escape/revert 对应）
     */
    public static final byte PKG_DELIMITER = 0x7e;

    // ====================== 终端上行（设备 -> 平台） ======================

    /** 0x0001：终端通用应答（设备对平台下发消息的 ACK） */
    public static final short TERMINAL_RESP_COMMON = 0x0001;

    /** 0x0002：终端心跳 */
    public static final short TERMINAL_MSG_HEARTBEAT = 0x0002;

    /** 0x0100：终端注册（首次注册/重新注册） */
    public static final short TERMINAL_MSG_REGISTER = 0x0100;

    /** 0x0003：终端注销 */
    public static final short TERMINAL_MSG_LOGOUT = 0x0003;

    /** 0x0102：终端鉴权（携带鉴权码） */
    public static final short TERMINAL_MSG_AUTH = 0x0102;

    /** 0x0200：位置信息上报 */
    public static final short TERMINAL_MSG_LOCATION = 0x0200;

    /** 0x0900：数据上报/透传（你们自定义扩展上报） */
    public static final short TERMINAL_MSG_REPORT = (short) 0x0900;

    /** 0x8900：平台下发/透传（你们自定义扩展下发） */
    public static final short TERMINAL_MSG_TRANS = (short) 0x8900;

    // ====================== 平台下行（平台 -> 终端） ======================

    /** 0x8001：平台通用应答（对终端上行的应答） */
    public static final short SERVER_RESP_COMMON = (short) 0x8001;

    /** 0x8100：终端注册应答（下发鉴权码等） */
    public static final short SERVER_RESP_REGISTER = (short) 0x8100;
}

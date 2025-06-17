package com.yunji.test;

import com.google.common.collect.Lists;
import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.util.ImeiHexUtil;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;


public class Jt808Test {


    private static final byte START_END_FLAG = 0x7E;

    static String imei = "865445078687176";
    // 消息流水号
    static short messageSerialNumber = 0;

    @Before
    public void init() throws Exception {
        imei = "865445078687176";
        messageSerialNumber = 1;
    }

    // CRC校验方法
    public static byte calculateChecksum(byte[] data, int start, int end) {
        byte crc = 0;
        for (int i = start; i <= end; i++) {
            crc ^= data[i];
        }
        return crc;
    }

    // 编码终端注册消息
    public static byte[] encodeMessage(byte[] body, short messageId, String phoneNumber, short messageSerialNumber) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 帧头
        buffer.put(START_END_FLAG);

        // 消息头部分
        buffer.putShort(messageId);  // 消息ID，2字节
        buffer.putShort((short) body.length);  // 消息体属性，2字节

        // 将手机号转成BCD格式的6字节
        // byte[] phoneBCD = ImeiUtil.imeiToByte(phoneNumber);
        phoneNumber = phoneNumber.substring(0, 14);
        byte[] phoneBCD = hexStringToBytes(ImeiHexUtil.convertToHexAndCompress(phoneNumber));
        buffer.put(phoneBCD);

        buffer.putShort(messageSerialNumber);  // 消息流水号，2字节

        // 消息体部分
        buffer.put(body);

        // 校验码
        byte[] rawData = Arrays.copyOfRange(buffer.array(), 1, buffer.position());
        byte checksum = calculateChecksum(rawData, 0, rawData.length - 1);
        buffer.put(checksum);

        // 帧尾
        buffer.put(START_END_FLAG);

        // 返回完整的消息数据
        return Arrays.copyOf(buffer.array(), buffer.position());
    }

    // 将手机号转成BCD码（每2个数字占1个字节）
//    public static byte[] phoneNumberToBCD(String phoneNumber) {
//        // 例如手机号: 123456789012，转成6字节: [0x12, 0x34, 0x56, 0x78, 0x90, 0x12]
//        int len = phoneNumber.length();
//        byte[] bcd = new byte[6];
//        for (int i = 0; i < len; i += 2) {
//            String subStr = phoneNumber.substring(i, Math.min(i + 2, len));
//            bcd[i / 2] = (byte) Integer.parseInt(subStr, 16);
//        }
//        return bcd;
//    }

    public static byte[] hexStringToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("16进制字符串长度必须为偶数");
        }
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            int index = i * 2;
            result[i] = (byte) Integer.parseInt(hex.substring(index, index + 2), 16);
        }
        return result;
    }

    // 将字节数组转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02X", b);  // 将每个字节转成2位十六进制
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getEncodeStr(byte[] messageBody, short messageId) {
        // 编码消息
        byte[] encodedMessage = encodeMessage(messageBody, messageId, imei, messageSerialNumber);
        // 将编码后的字节数组转换成十六进制字符串
        return bytesToHex(encodedMessage);
    }

    public static String getEncodeStr(byte[] messageBody, short messageId, String inputImei) {
        // 编码消息
        byte[] encodedMessage = encodeMessage(messageBody, messageId, inputImei, messageSerialNumber);
        // 将编码后的字节数组转换成十六进制字符串
        return bytesToHex(encodedMessage);
    }

    @Test
    public void testHeart() {
        byte[] messageBody = {};
        // 消息ID：0x0002 (心跳)
        short messageId = JT808Const.TERMINAL_MSG_AUTH;
        System.out.println(getEncodeStr(messageBody, messageId));
    }

    @Test
    public void testAuth() {
        // 消息ID：0x0102 (鉴权)
        short messageId = JT808Const.TERMINAL_MSG_AUTH;
        ByteBuffer payload = ByteBuffer.allocate(6);
        payload.put("123456".getBytes(JT808Const.DEFAULT_CHARSET));
        byte[] messageBody = payload.array();
        System.out.println(getEncodeStr(messageBody, messageId));
    }

    public static void generateAuthCode(String imei) {
        // 消息ID：0x0102 (鉴权)
        short messageId = JT808Const.TERMINAL_MSG_AUTH;
        ByteBuffer payload = ByteBuffer.allocate(6);
        payload.put("123456".getBytes(JT808Const.DEFAULT_CHARSET));
        byte[] messageBody = payload.array();
        System.out.println(getEncodeStr(messageBody, messageId, imei));
    }

    public static void main(String[] args) {
        ArrayList<String> strings = Lists.newArrayList("867539020148362",
                "356789041237654",
                "490154203237518",
                "353509104582733",
                "861181041398275",
                "864789032541660",
                "352099109674520",
                "354982063297148",
                "869532040117839",
                "356789043216790");
        for (String string : strings) {
            generateAuthCode(string);
        }
    }

    @Test
    public void testRegister() {
        short provinceId = 1;
        short cityId = 2;
        String manufacturerId = "ABCDE"; // 5字节
        String terminalType = "12345678"; // 8字节
        String terminalId = "TERM123"; // 7字节
        byte licensePlateColor = 1; // 1字节
        String licensePlate = "110110110110"; // 剩余字节
        ByteBuffer payload = ByteBuffer.allocate(2 + 2 + 5 + 8 + 7 + 1 + licensePlate.getBytes(JT808Const.DEFAULT_CHARSET).length);
        System.out.println("Capacity: " + payload.capacity());
        System.out.println("licensePlate: " + licensePlate.getBytes(JT808Const.DEFAULT_CHARSET).length);
        payload.putShort(provinceId);
        payload.putShort(cityId);
        payload.put(manufacturerId.getBytes(JT808Const.DEFAULT_CHARSET));
        payload.put(terminalType.getBytes(JT808Const.DEFAULT_CHARSET));
        payload.put(terminalId.getBytes(JT808Const.DEFAULT_CHARSET));
        payload.put(licensePlateColor);
        payload.put(licensePlate.getBytes(JT808Const.DEFAULT_CHARSET));
        byte[] messageBody = payload.array();
        // 消息ID：0x0100 (注册)
        short messageId = JT808Const.TERMINAL_MSG_REGISTER;
        System.out.println(getEncodeStr(messageBody, messageId));
    }

//    @Test
//    public void testReport() {
//        // 消息ID：0x0900 上报状态
//        byte messageType = 1;
//        String messageLength = "ABCD";
//        short packageLength = 1;
//        String messageContent = "OK";
//        ByteBuffer payload = ByteBuffer.allocate(1 + 4 + 2 + messageContent.getBytes(JT808Const.DEFAULT_CHARSET).length);
//        System.out.println("Capacity: " + payload.capacity());
//        System.out.println("messageContent: " + messageContent.getBytes(JT808Const.DEFAULT_CHARSET).length);
//        payload.put(messageType);
//        payload.put(messageLength.getBytes(JT808Const.DEFAULT_CHARSET));
//        payload.putShort(packageLength);
//        payload.put(messageContent.getBytes(JT808Const.DEFAULT_CHARSET));
//        byte[] messageBody = payload.array();
//        short messageId = JT808Const.TERMINAL_MSG_REPORT;
//        System.out.println(getEncodeStr(messageBody, messageId));
//    }


}

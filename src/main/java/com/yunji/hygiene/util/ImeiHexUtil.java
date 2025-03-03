package com.yunji.hygiene.util;

import java.util.Arrays;

/**
 * @author : peter-zhu
 * @date : 2025/1/20 16:42
 * @description : TODO
 **/
public class ImeiHexUtil {

    public static String convertToHexAndCompress(String numStr) {
        long num = Long.parseLong(numStr);  // 将数字字符串转为长整型
        String hexStr = Long.toHexString(num).toUpperCase(); // 转为16进制字符串，转换为大写
        if (hexStr.length() < 12) {
            hexStr = String.format("%12s", hexStr).replace(' ', '0'); // 填充零
        }
        return hexStr.substring(0, 12); // 截取前12位
    }

    public static byte[] hexStringToByteArray(String hexStr) {
        int length = hexStr.length();
        byte[] byteArray = new byte[length / 2]; // 每两个字符对应一个字节

        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i + 1), 16));
        }

        return byteArray;
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));  // 每个字节转为2位16进制
        }
        return hexString.toString();
    }

    public static String hexStrToStr(String hexString) {
        return String.valueOf(Long.parseLong(hexString, 16));
    }

    public static void main(String[] args) {
        // 示例：14位数字字符串
        String numStr = "860137073765025";
        // 转换并输出6字节16进制
        String result = convertToHexAndCompress(numStr);
        System.out.println("result " + result);
        byte[] bytes = hexStringToByteArray(result);
        System.out.println("Compressed 6-byte Hex: " + Arrays.toString(bytes)); // 输出 6字节16进制表示
        System.out.println(hexStrToStr(byteToHexString(bytes)));
    }
}

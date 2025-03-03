package com.yunji.hygiene.util;

import java.nio.charset.StandardCharsets;

/**
 * @author : peter-zhu
 * @date : 2025/2/19 16:28
 * @description : TODO
 **/
public class AsciiUtil {

    public static String toStr(byte[] bytes) {
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    public static byte[] toByte(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }
}

package com.yunji.hygiene.util;

import com.yunji.hygiene.entity.domain.DeviceException;
import com.yunji.hygiene.entity.enums.DeviceErrorEnum;

/**
 * @author : peter-zhu
 * @date : 2025/1/20 14:59
 * @description : TODO
 **/
public class Luhn {
    // 计算 IMEI 校验码的函数
    public static int calculateCheckDigit(String imeiWithoutCheckDigit) {
        // 校验输入是否合法
        if (imeiWithoutCheckDigit == null || imeiWithoutCheckDigit.length() != 14) {
            throw new DeviceException(DeviceErrorEnum.CHECKED_202505, imeiWithoutCheckDigit);
        }
        int sum = 0;
        // 从左到右遍历前14位数字
        for (int i = 0; i < imeiWithoutCheckDigit.length(); i++) {
            int digit = imeiWithoutCheckDigit.charAt(i) - '0'; // 将字符转换为数字
            // 如果是偶数位（0-based），乘以2
            if (i % 2 != 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9; // 如果结果大于9，则各位相加
                }
            }
            sum += digit;
        }
        return (10 - (sum % 10)) % 10;
    }

    public static void main(String[] args) {
        // 示例：IMEI 前 14 位
        String imeiWithoutCheckDigit = "86013707390090";
        // 计算校验码
        int checkDigit = calculateCheckDigit(imeiWithoutCheckDigit);
        // 输出完整的 IMEI（包含校验码）
        String fullImei = imeiWithoutCheckDigit + checkDigit;
        System.out.println("Complete IMEI: " + fullImei);
    }
}

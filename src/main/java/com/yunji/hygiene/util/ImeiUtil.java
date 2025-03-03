package com.yunji.hygiene.util;

import com.yunji.hygiene.entity.domain.DeviceException;
import com.yunji.hygiene.entity.enums.DeviceErrorEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author : peter-zhu
 * @date : 2025/1/21 11:25
 * @description : TODO
 **/
public class ImeiUtil {
    public static void validateImei(String imei) {
        if (StringUtils.isEmpty(imei) || imei.length() != 15)
            throw new DeviceException(DeviceErrorEnum.CHECKED_202505, imei);
    }

    public static String byteToImei(byte[] bytes) {
        String hexString = ImeiHexUtil.byteToHexString(bytes);
        String str = ImeiHexUtil.hexStrToStr(hexString);
        return str + Luhn.calculateCheckDigit(str);
    }

    public static byte[] imeiToByte(String imei) {
        validateImei(imei);
        imei = imei.substring(0, 14);
        String hex = ImeiHexUtil.convertToHexAndCompress(imei);
        return ImeiHexUtil.hexStringToByteArray(hex);
    }

    public static void main(String[] args) {
        String imei = "860137073765025";
        byte[] bytes = imeiToByte(imei);
        System.out.println(Arrays.toString(bytes));
        String imei1 = byteToImei(bytes);
        System.out.println(imei1);
    }
}

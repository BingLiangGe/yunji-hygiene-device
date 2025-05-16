package com.yunji.hygiene.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author : peter-zhu
 * @date : 2025/5/15 18:27
 * @description : TODO
 **/
@Slf4j
public class DeviceSignatureUtil {

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static String generateSignature(Long timestamp, String nonce, String body, String secret) {
        if (body == null)
            body = "";
        String contentToSign = timestamp + nonce + body;
//        log.debug("DeviceSignatureUtil generateSignature sign:{}",contentToSign);
//        log.debug("DeviceSignatureUtil generateSignature nonce:{}",nonce);
//        log.debug("DeviceSignatureUtil generateSignature body:{}",body);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(contentToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("DeviceSignatureUtil generateSignature error,data:{}", body, e);
        }
        return null;
    }
}

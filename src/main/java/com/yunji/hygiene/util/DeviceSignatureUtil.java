package com.yunji.hygiene.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author : peter-zhu
 * @date : 2025/5/15 18:27
 * @description : TODO
 **/
public class DeviceSignatureUtil {

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static String generateSignature(Long timestamp, String nonce, String body, String secret) throws Exception {
        String contentToSign = timestamp + "\n" + nonce + "\n" + body;
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] hash = mac.doFinal(contentToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}

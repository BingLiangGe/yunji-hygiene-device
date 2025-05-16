package com.yunji.hygiene.config;

import com.yunji.hygiene.util.SignatureUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author : peter-zhu
 * @date : 2025/5/16 10:29
 * @description : TODO
 **/
public class HmacAuthFilter extends OncePerRequestFilter {
    private static final String SECRET = "";

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws IOException {
        try {
            String timestamp = request.getHeader("X-Device-Timestamp");
            String nonce = request.getHeader("X-Device-Nonce");
            String signature = request.getHeader("X-Device-Signature");
            if (timestamp == null || nonce == null || signature == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing signature headers");
                return;
            }
            // 时间戳校验
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - ts) > 300) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Request expired");
                return;
            }
            // 读取请求体
            String body = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines().collect(Collectors.joining());
            // 重新计算签名
            String expected = SignatureUtil.generateSignature(ts, nonce, body, SECRET);
            if (!expected.equals(signature)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid signature");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Filter error");
        }
    }
}

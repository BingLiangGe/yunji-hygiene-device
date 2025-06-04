package com.yunji.hygiene.config;

import com.yunji.hygiene.util.DeviceSignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * @author : peter-zhu
 * @date : 2025/5/16 10:29
 * @description : TODO
 **/
@Component
@Slf4j
@Profile("prod")
public class HmacAuthFilter extends OncePerRequestFilter {
    private static final String SECRET = "hmac-secret-hygiene-device";

    @Resource
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws IOException {
        try {
            HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
            if (handlerExecutionChain != null) {
                Object handler = handlerExecutionChain.getHandler();
                if (handler instanceof HandlerMethod) {
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
                    if (handlerMethod.hasMethodAnnotation(HMACAuth.class))
                        doRequest(request, response, filterChain);
                    else
                        filterChain.doFilter(request, response);
                }
            }
        } catch (Exception e) {
            log.error("HmacAuthFilter exceeds expected error msg:{}", e.getMessage(), e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Filter error");
        }
    }

    private void doRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {
        String timestamp = request.getHeader("X-Device-Timestamp");
        String nonce = request.getHeader("X-Device-Nonce");
        String signature = request.getHeader("X-Device-Signature");
        if (timestamp == null || nonce == null || signature == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing signature headers");
            return;
        }
        long ts = Long.parseLong(timestamp);
        long now = System.currentTimeMillis() / 1000;
        long abs = Math.abs(now - ts);
        log.info("HmacAuthFilter Request in method:{},diffTime:{}", signature, abs);
        if (abs > 300) {
            log.error("HmacAuthFilter Request expired signature:{}", signature);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Request expired");
            return;
        }
        String body = new BufferedReader(new InputStreamReader(request.getInputStream()))
                .lines().collect(Collectors.joining());
        String expected = DeviceSignatureUtil.generateSignature(ts, nonce, body, SECRET);
        log.info("HmacAuthFilter Request in expected:{}", signature);
        if (!signature.equals(expected)) {
            log.error("HmacAuthFilter exceeds expected signature:{}, actual signature:{}", expected, signature);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid signature");
            return;
        }
        HttpServletRequest wrappedRequest = wrapRequestWithBody(request, body);
        filterChain.doFilter(wrappedRequest, response);
    }

    private HttpServletRequest wrapRequestWithBody(HttpServletRequest request, String body) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public ServletInputStream getInputStream() {
                ByteArrayInputStream bi = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
                return new ServletInputStream() {
                    @Override
                    public int read() {
                        return bi.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return bi.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener listener) {
                    }
                };
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
            }
        };
    }
}

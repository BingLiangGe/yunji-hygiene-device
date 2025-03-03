package com.yunji.hygiene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 服务器启动在NettyTcpServer中
 * @Version: 1.0
 */
@SpringBootApplication
public class HygieneDeviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HygieneDeviceApplication.class, args);
    }
}

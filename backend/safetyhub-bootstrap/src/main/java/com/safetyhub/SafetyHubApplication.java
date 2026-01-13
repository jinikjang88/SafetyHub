package com.safetyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SafetyHub 메인 애플리케이션
 * 통합 안전 관제 플랫폼
 */
@SpringBootApplication
@EnableScheduling
public class SafetyHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafetyHubApplication.class, args);
    }
}

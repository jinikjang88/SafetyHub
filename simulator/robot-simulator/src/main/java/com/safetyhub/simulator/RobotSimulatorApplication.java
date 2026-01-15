package com.safetyhub.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 로봇 시뮬레이터 애플리케이션
 * SafetyHub 시스템 검증을 위한 대규모 로봇 시뮬레이션 실행
 */
@SpringBootApplication
@EnableScheduling
public class RobotSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotSimulatorApplication.class, args);
    }
}

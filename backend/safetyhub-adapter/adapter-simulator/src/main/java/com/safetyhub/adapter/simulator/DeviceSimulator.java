package com.safetyhub.adapter.simulator;

import com.safetyhub.application.device.DeviceControlUseCase;
import com.safetyhub.core.domain.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * 장치 시뮬레이터
 * 개발/테스트용 가상 장치 데이터 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceSimulator {

    private final DeviceControlUseCase deviceControlUseCase;
    private final Random random = new Random();

    private boolean enabled = false;

    /**
     * 시뮬레이션 시작
     */
    public void start() {
        this.enabled = true;
        log.info("Device simulator started");
    }

    /**
     * 시뮬레이션 중지
     */
    public void stop() {
        this.enabled = false;
        log.info("Device simulator stopped");
    }

    /**
     * 주기적 하트비트 전송 (5초마다)
     */
    @Scheduled(fixedRate = 5000)
    public void simulateHeartbeats() {
        if (!enabled) return;

        List<Device> devices = deviceControlUseCase.getAllDevices();
        devices.forEach(device -> {
            deviceControlUseCase.processHeartbeat(device.getDeviceId());
            log.debug("Simulated heartbeat for device: {}", device.getDeviceId());
        });
    }

    /**
     * 랜덤 센서 데이터 생성
     */
    public SimulatedSensorData generateSensorData(String deviceId) {
        return SimulatedSensorData.builder()
                .deviceId(deviceId)
                .temperature(20 + random.nextDouble() * 15)  // 20-35도
                .humidity(40 + random.nextDouble() * 40)     // 40-80%
                .gasLevel(random.nextDouble() * 100)         // 0-100 ppm
                .vibration(random.nextDouble() * 10)         // 0-10 mm/s
                .noise(60 + random.nextDouble() * 40)        // 60-100 dB
                .build();
    }

    @lombok.Builder
    @lombok.Getter
    public static class SimulatedSensorData {
        private String deviceId;
        private double temperature;
        private double humidity;
        private double gasLevel;
        private double vibration;
        private double noise;
    }
}

package com.safetyhub.adapter.mqtt;

import com.safetyhub.application.device.DeviceControlUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MQTT 메시지 핸들러
 * IoT 장치로부터 수신된 메시지 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttMessageHandler {

    private final DeviceControlUseCase deviceControlUseCase;

    /**
     * 장치 하트비트 처리
     * Topic: safetyhub/devices/{deviceId}/heartbeat
     */
    public void handleHeartbeat(String deviceId, String payload) {
        log.debug("Received heartbeat from device: {}", deviceId);
        deviceControlUseCase.processHeartbeat(deviceId);
    }

    /**
     * 센서 데이터 처리
     * Topic: safetyhub/devices/{deviceId}/sensors
     */
    public void handleSensorData(String deviceId, String payload) {
        log.debug("Received sensor data from device: {} - {}", deviceId, payload);
        // TODO: 센서 데이터 파싱 및 저장
    }

    /**
     * 긴급 상황 감지 처리
     * Topic: safetyhub/devices/{deviceId}/emergency
     */
    public void handleEmergency(String deviceId, String payload) {
        log.warn("Emergency detected from device: {} - {}", deviceId, payload);
        // TODO: 긴급 상황 UseCase 호출
    }

    /**
     * 작업자 위치 업데이트 처리
     * Topic: safetyhub/workers/{workerId}/location
     */
    public void handleWorkerLocation(String workerId, String payload) {
        log.debug("Received location update from worker: {} - {}", workerId, payload);
        // TODO: 위치 데이터 파싱 및 UseCase 호출
    }
}

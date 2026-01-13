package com.safetyhub.application.device;

import com.safetyhub.core.domain.Device;

import java.util.List;
import java.util.Optional;

/**
 * 설비 제어 UseCase 인터페이스
 */
public interface DeviceControlUseCase {

    /**
     * 장치 등록
     */
    Device registerDevice(RegisterDeviceCommand command);

    /**
     * 장치 상태 업데이트
     */
    Device updateDeviceStatus(String deviceId, Device.DeviceStatus status);

    /**
     * 장치 조회
     */
    Optional<Device> getDevice(String deviceId);

    /**
     * 구역별 장치 목록 조회
     */
    List<Device> getDevicesByZone(String zoneId);

    /**
     * 전체 장치 목록 조회
     */
    List<Device> getAllDevices();

    /**
     * 하트비트 처리
     */
    void processHeartbeat(String deviceId);

    /**
     * 장치 등록 커맨드
     */
    record RegisterDeviceCommand(
            String deviceId,
            String name,
            Device.DeviceType type,
            String zoneId,
            Double latitude,
            Double longitude
    ) {}
}

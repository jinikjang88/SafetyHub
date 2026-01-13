package com.safetyhub.adapter.rest;

import com.safetyhub.application.device.DeviceControlUseCase;
import com.safetyhub.core.domain.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장치 관리 REST API
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceControlUseCase deviceControlUseCase;

    @PostMapping
    public ResponseEntity<Device> registerDevice(
            @RequestBody DeviceControlUseCase.RegisterDeviceCommand command) {
        Device device = deviceControlUseCase.registerDevice(command);
        return ResponseEntity.ok(device);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDevice(@PathVariable String deviceId) {
        return deviceControlUseCase.getDevice(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceControlUseCase.getAllDevices());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Device>> getDevicesByZone(@PathVariable String zoneId) {
        return ResponseEntity.ok(deviceControlUseCase.getDevicesByZone(zoneId));
    }

    @PatchMapping("/{deviceId}/status")
    public ResponseEntity<Device> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestParam Device.DeviceStatus status) {
        Device device = deviceControlUseCase.updateDeviceStatus(deviceId, status);
        return ResponseEntity.ok(device);
    }

    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<Void> processHeartbeat(@PathVariable String deviceId) {
        deviceControlUseCase.processHeartbeat(deviceId);
        return ResponseEntity.ok().build();
    }
}

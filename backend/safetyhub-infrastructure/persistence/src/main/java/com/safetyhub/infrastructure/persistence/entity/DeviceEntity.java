package com.safetyhub.infrastructure.persistence.entity;

import com.safetyhub.core.domain.Device;
import com.safetyhub.core.domain.Location;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 장치 JPA 엔티티
 */
@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Device.DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Device.DeviceStatus status;

    @Column(name = "zone_id")
    private String zoneId;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "floor")
    private String floor;

    @Column(name = "section")
    private String section;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 도메인 객체로 변환
     */
    public Device toDomain() {
        return Device.builder()
                .id(this.id)
                .deviceId(this.deviceId)
                .name(this.name)
                .type(this.type)
                .status(this.status)
                .zoneId(this.zoneId)
                .location(Location.builder()
                        .latitude(this.latitude)
                        .longitude(this.longitude)
                        .altitude(this.altitude)
                        .floor(this.floor)
                        .section(this.section)
                        .build())
                .lastHeartbeat(this.lastHeartbeat)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 도메인 객체에서 생성
     */
    public static DeviceEntity fromDomain(Device device) {
        Location location = device.getLocation();
        return DeviceEntity.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .type(device.getType())
                .status(device.getStatus())
                .zoneId(device.getZoneId())
                .latitude(location != null ? location.getLatitude() : null)
                .longitude(location != null ? location.getLongitude() : null)
                .altitude(location != null ? location.getAltitude() : null)
                .floor(location != null ? location.getFloor() : null)
                .section(location != null ? location.getSection() : null)
                .lastHeartbeat(device.getLastHeartbeat())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

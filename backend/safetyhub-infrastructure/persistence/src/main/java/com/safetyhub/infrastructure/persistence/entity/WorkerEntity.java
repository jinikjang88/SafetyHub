package com.safetyhub.infrastructure.persistence.entity;

import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 작업자 JPA 엔티티
 */
@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_id", unique = true, nullable = false)
    private String workerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "department")
    private String department;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "device_id")
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Worker.WorkerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    private Worker.HealthStatus healthStatus;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_zone_id")
    private String currentZoneId;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 도메인 객체로 변환
     */
    public Worker toDomain() {
        return Worker.builder()
                .id(this.id)
                .workerId(this.workerId)
                .name(this.name)
                .department(this.department)
                .phoneNumber(this.phoneNumber)
                .deviceId(this.deviceId)
                .status(this.status)
                .healthStatus(this.healthStatus)
                .currentLocation(Location.builder()
                        .latitude(this.currentLatitude)
                        .longitude(this.currentLongitude)
                        .build())
                .currentZoneId(this.currentZoneId)
                .lastActiveAt(this.lastActiveAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 도메인 객체에서 생성
     */
    public static WorkerEntity fromDomain(Worker worker) {
        Location location = worker.getCurrentLocation();
        return WorkerEntity.builder()
                .id(worker.getId())
                .workerId(worker.getWorkerId())
                .name(worker.getName())
                .department(worker.getDepartment())
                .phoneNumber(worker.getPhoneNumber())
                .deviceId(worker.getDeviceId())
                .status(worker.getStatus())
                .healthStatus(worker.getHealthStatus())
                .currentLatitude(location != null ? location.getLatitude() : null)
                .currentLongitude(location != null ? location.getLongitude() : null)
                .currentZoneId(worker.getCurrentZoneId())
                .lastActiveAt(worker.getLastActiveAt())
                .createdAt(worker.getCreatedAt())
                .updatedAt(worker.getUpdatedAt())
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

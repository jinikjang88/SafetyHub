package com.safetyhub.infrastructure.persistence.entity;

import com.safetyhub.core.domain.Emergency;
import com.safetyhub.core.domain.Location;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 긴급 상황 JPA 엔티티
 */
@Entity
@Table(name = "emergencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emergency_id", unique = true, nullable = false)
    private String emergencyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Emergency.EmergencyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Emergency.EmergencyLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Emergency.EmergencyStatus status;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "zone_id")
    private String zoneId;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "reporter_id")
    private String reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporter_type")
    private Emergency.ReporterType reporterType;

    @Column(name = "affected_worker_ids", length = 2000)
    private String affectedWorkerIds;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolution", length = 1000)
    private String resolution;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 도메인 객체로 변환
     */
    public Emergency toDomain() {
        List<String> workerIds = null;
        if (this.affectedWorkerIds != null && !this.affectedWorkerIds.isEmpty()) {
            workerIds = List.of(this.affectedWorkerIds.split(","));
        }

        return Emergency.builder()
                .id(this.id)
                .emergencyId(this.emergencyId)
                .type(this.type)
                .level(this.level)
                .status(this.status)
                .description(this.description)
                .zoneId(this.zoneId)
                .location(Location.builder()
                        .latitude(this.latitude)
                        .longitude(this.longitude)
                        .build())
                .reporterId(this.reporterId)
                .reporterType(this.reporterType)
                .affectedWorkerIds(workerIds)
                .occurredAt(this.occurredAt)
                .resolvedAt(this.resolvedAt)
                .resolvedBy(this.resolvedBy)
                .resolution(this.resolution)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 도메인 객체에서 생성
     */
    public static EmergencyEntity fromDomain(Emergency emergency) {
        Location location = emergency.getLocation();
        String workerIds = null;
        if (emergency.getAffectedWorkerIds() != null) {
            workerIds = String.join(",", emergency.getAffectedWorkerIds());
        }

        return EmergencyEntity.builder()
                .id(emergency.getId())
                .emergencyId(emergency.getEmergencyId())
                .type(emergency.getType())
                .level(emergency.getLevel())
                .status(emergency.getStatus())
                .description(emergency.getDescription())
                .zoneId(emergency.getZoneId())
                .latitude(location != null ? location.getLatitude() : null)
                .longitude(location != null ? location.getLongitude() : null)
                .reporterId(emergency.getReporterId())
                .reporterType(emergency.getReporterType())
                .affectedWorkerIds(workerIds)
                .occurredAt(emergency.getOccurredAt())
                .resolvedAt(emergency.getResolvedAt())
                .resolvedBy(emergency.getResolvedBy())
                .resolution(emergency.getResolution())
                .createdAt(emergency.getCreatedAt())
                .updatedAt(emergency.getUpdatedAt())
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

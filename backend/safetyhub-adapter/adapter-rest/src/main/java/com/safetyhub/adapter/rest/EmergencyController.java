package com.safetyhub.adapter.rest;

import com.safetyhub.application.emergency.EmergencyResponseUseCase;
import com.safetyhub.core.domain.Emergency;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 긴급 상황 관리 REST API
 */
@RestController
@RequestMapping("/api/v1/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyResponseUseCase emergencyResponseUseCase;

    @PostMapping
    public ResponseEntity<Emergency> createEmergency(
            @RequestBody EmergencyResponseUseCase.CreateEmergencyCommand command) {
        Emergency emergency = emergencyResponseUseCase.createEmergency(command);
        return ResponseEntity.ok(emergency);
    }

    @GetMapping("/{emergencyId}")
    public ResponseEntity<Emergency> getEmergency(@PathVariable String emergencyId) {
        return emergencyResponseUseCase.getEmergency(emergencyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Emergency>> getActiveEmergencies() {
        return ResponseEntity.ok(emergencyResponseUseCase.getActiveEmergencies());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Emergency>> getEmergenciesByZone(@PathVariable String zoneId) {
        return ResponseEntity.ok(emergencyResponseUseCase.getEmergenciesByZone(zoneId));
    }

    @PatchMapping("/{emergencyId}/status")
    public ResponseEntity<Emergency> updateEmergencyStatus(
            @PathVariable String emergencyId,
            @RequestParam Emergency.EmergencyStatus status) {
        Emergency emergency = emergencyResponseUseCase.updateEmergencyStatus(emergencyId, status);
        return ResponseEntity.ok(emergency);
    }

    @PostMapping("/{emergencyId}/resolve")
    public ResponseEntity<Emergency> resolveEmergency(
            @PathVariable String emergencyId,
            @RequestParam String resolvedBy,
            @RequestParam String resolution) {
        Emergency emergency = emergencyResponseUseCase.resolveEmergency(emergencyId, resolvedBy, resolution);
        return ResponseEntity.ok(emergency);
    }

    @PostMapping("/{emergencyId}/evacuate")
    public ResponseEntity<Void> issueEvacuationOrder(
            @PathVariable String emergencyId,
            @RequestBody List<String> zoneIds) {
        emergencyResponseUseCase.issueEvacuationOrder(emergencyId, zoneIds);
        return ResponseEntity.ok().build();
    }
}

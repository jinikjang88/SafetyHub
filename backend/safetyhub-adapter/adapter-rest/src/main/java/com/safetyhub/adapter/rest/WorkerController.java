package com.safetyhub.adapter.rest;

import com.safetyhub.application.worker.WorkerMonitoringUseCase;
import com.safetyhub.core.domain.Worker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 작업자 관리 REST API
 */
@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerMonitoringUseCase workerMonitoringUseCase;

    @PostMapping
    public ResponseEntity<Worker> registerWorker(
            @RequestBody WorkerMonitoringUseCase.RegisterWorkerCommand command) {
        Worker worker = workerMonitoringUseCase.registerWorker(command);
        return ResponseEntity.ok(worker);
    }

    @GetMapping("/{workerId}")
    public ResponseEntity<Worker> getWorker(@PathVariable String workerId) {
        return workerMonitoringUseCase.getWorker(workerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Worker>> getAllWorkers() {
        return ResponseEntity.ok(workerMonitoringUseCase.getAllWorkers());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Worker>> getWorkersByZone(@PathVariable String zoneId) {
        return ResponseEntity.ok(workerMonitoringUseCase.getWorkersByZone(zoneId));
    }

    @GetMapping("/danger")
    public ResponseEntity<List<Worker>> getWorkersInDanger() {
        return ResponseEntity.ok(workerMonitoringUseCase.getWorkersInDanger());
    }

    @PatchMapping("/{workerId}/health-status")
    public ResponseEntity<Worker> updateHealthStatus(
            @PathVariable String workerId,
            @RequestParam Worker.HealthStatus healthStatus) {
        Worker worker = workerMonitoringUseCase.updateHealthStatus(workerId, healthStatus);
        return ResponseEntity.ok(worker);
    }
}

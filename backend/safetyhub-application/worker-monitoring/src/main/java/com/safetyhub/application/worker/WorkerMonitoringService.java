package com.safetyhub.application.worker;

import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.Worker;
import com.safetyhub.core.event.WorkerLocationUpdatedEvent;
import com.safetyhub.core.port.out.EventPublisher;
import com.safetyhub.core.port.out.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 작업자 모니터링 서비스 구현
 */
@Service
@RequiredArgsConstructor
public class WorkerMonitoringService implements WorkerMonitoringUseCase {

    private final WorkerRepository workerRepository;
    private final EventPublisher eventPublisher;

    @Override
    public Worker registerWorker(RegisterWorkerCommand command) {
        Worker worker = Worker.builder()
                .workerId(command.workerId())
                .name(command.name())
                .department(command.department())
                .phoneNumber(command.phoneNumber())
                .deviceId(command.deviceId())
                .status(Worker.WorkerStatus.OFF_DUTY)
                .healthStatus(Worker.HealthStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return workerRepository.save(worker);
    }

    @Override
    public Worker updateWorkerLocation(String workerId, Location location, String zoneId) {
        Worker worker = workerRepository.findByWorkerId(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found: " + workerId));

        Location previousLocation = worker.getCurrentLocation();
        String previousZoneId = worker.getCurrentZoneId();

        worker.updateLocation(location, zoneId);
        Worker savedWorker = workerRepository.save(worker);

        // 위치 변경 이벤트 발행
        eventPublisher.publish(WorkerLocationUpdatedEvent.create(
                workerId,
                previousLocation,
                location,
                previousZoneId,
                zoneId,
                worker.getHealthStatus()
        ));

        return savedWorker;
    }

    @Override
    public Worker updateHealthStatus(String workerId, Worker.HealthStatus healthStatus) {
        Worker worker = workerRepository.findByWorkerId(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found: " + workerId));

        Worker updatedWorker = Worker.builder()
                .id(worker.getId())
                .workerId(worker.getWorkerId())
                .name(worker.getName())
                .department(worker.getDepartment())
                .phoneNumber(worker.getPhoneNumber())
                .deviceId(worker.getDeviceId())
                .status(worker.getStatus())
                .healthStatus(healthStatus)
                .currentLocation(worker.getCurrentLocation())
                .currentZoneId(worker.getCurrentZoneId())
                .lastActiveAt(LocalDateTime.now())
                .createdAt(worker.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        return workerRepository.save(updatedWorker);
    }

    @Override
    public Optional<Worker> getWorker(String workerId) {
        return workerRepository.findByWorkerId(workerId);
    }

    @Override
    public List<Worker> getWorkersByZone(String zoneId) {
        return workerRepository.findByZoneId(zoneId);
    }

    @Override
    public List<Worker> getWorkersInDanger() {
        return workerRepository.findWorkersInDanger();
    }

    @Override
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }
}

package com.safetyhub.core.port.out;

import com.safetyhub.core.domain.Worker;

import java.util.List;
import java.util.Optional;

/**
 * 작업자 저장소 출력 포트
 */
public interface WorkerRepository {

    Worker save(Worker worker);

    Optional<Worker> findById(Long id);

    Optional<Worker> findByWorkerId(String workerId);

    Optional<Worker> findByDeviceId(String deviceId);

    List<Worker> findByZoneId(String zoneId);

    List<Worker> findByStatus(Worker.WorkerStatus status);

    List<Worker> findByHealthStatus(Worker.HealthStatus healthStatus);

    List<Worker> findAll();

    void deleteById(Long id);

    int countByZoneId(String zoneId);

    List<Worker> findWorkersInDanger();
}

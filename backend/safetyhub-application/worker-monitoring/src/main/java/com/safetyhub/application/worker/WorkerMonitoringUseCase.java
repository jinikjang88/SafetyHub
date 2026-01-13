package com.safetyhub.application.worker;

import com.safetyhub.core.domain.Location;
import com.safetyhub.core.domain.Worker;

import java.util.List;
import java.util.Optional;

/**
 * 작업자 모니터링 UseCase 인터페이스
 */
public interface WorkerMonitoringUseCase {

    /**
     * 작업자 등록
     */
    Worker registerWorker(RegisterWorkerCommand command);

    /**
     * 작업자 위치 업데이트
     */
    Worker updateWorkerLocation(String workerId, Location location, String zoneId);

    /**
     * 작업자 건강 상태 업데이트
     */
    Worker updateHealthStatus(String workerId, Worker.HealthStatus healthStatus);

    /**
     * 작업자 조회
     */
    Optional<Worker> getWorker(String workerId);

    /**
     * 구역별 작업자 목록 조회
     */
    List<Worker> getWorkersByZone(String zoneId);

    /**
     * 위험 상태 작업자 조회
     */
    List<Worker> getWorkersInDanger();

    /**
     * 전체 작업자 조회
     */
    List<Worker> getAllWorkers();

    /**
     * 작업자 등록 커맨드
     */
    record RegisterWorkerCommand(
            String workerId,
            String name,
            String department,
            String phoneNumber,
            String deviceId
    ) {}
}

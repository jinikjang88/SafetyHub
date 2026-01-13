package com.safetyhub.simulator.event;

import com.safetyhub.simulator.core.RobotState;

import java.util.Random;

/**
 * 센서 데이터 생성기
 * SafetyKit/LifeGuard 센서 데이터를 시뮬레이션
 */
public class SensorDataGenerator {
    private final Random random = new Random();

    /**
     * 심박수 생성 (bpm)
     */
    public int generateHeartRate(RobotState state) {
        int base = switch (state) {
            case WORKING -> 85;
            case MOVING, EVACUATING -> 100;
            case RESTING, EATING, IDLE -> 70;
            case EMERGENCY -> 130;
            case CHARGING -> 65;
        };
        return base + random.nextInt(20) - 10;
    }

    /**
     * 체온 생성 (°C)
     */
    public double generateBodyTemperature(RobotState state) {
        double base = switch (state) {
            case WORKING, MOVING -> 36.8;
            case EMERGENCY -> 37.5;
            default -> 36.5;
        };
        return base + (random.nextDouble() * 0.6 - 0.3);
    }

    /**
     * 가속도계 데이터 생성 (G)
     */
    public AccelerometerData generateAccelerometer(RobotState state) {
        double baseX, baseY, baseZ;

        switch (state) {
            case MOVING, EVACUATING -> {
                baseX = random.nextDouble() * 0.5;
                baseY = random.nextDouble() * 0.5;
                baseZ = 1.0 + random.nextDouble() * 0.2;
            }
            case EMERGENCY -> {
                // 낙상 시뮬레이션
                baseX = random.nextDouble() * 2.0;
                baseY = random.nextDouble() * 2.0;
                baseZ = random.nextDouble() * 0.5;
            }
            default -> {
                baseX = random.nextDouble() * 0.1;
                baseY = random.nextDouble() * 0.1;
                baseZ = 1.0 + random.nextDouble() * 0.05;
            }
        }

        return new AccelerometerData(baseX, baseY, baseZ);
    }

    /**
     * 환경 센서 데이터 생성
     */
    public EnvironmentData generateEnvironment(boolean hazardous) {
        double temperature = hazardous ? 45 + random.nextDouble() * 20 : 22 + random.nextDouble() * 5;
        double humidity = 40 + random.nextDouble() * 30;
        int co2 = hazardous ? 2000 + random.nextInt(3000) : 400 + random.nextInt(400);
        boolean smoke = hazardous && random.nextDouble() > 0.3;
        double gasLevel = hazardous ? random.nextDouble() * 100 : random.nextDouble() * 5;

        return new EnvironmentData(temperature, humidity, co2, smoke, gasLevel);
    }

    /**
     * PPG (Photoplethysmography) 신호 생성
     */
    public int[] generatePpgSignal(int samples) {
        int[] signal = new int[samples];
        for (int i = 0; i < samples; i++) {
            // 심박 파형 시뮬레이션
            double t = i * 0.01; // 100Hz
            double heartbeat = Math.sin(2 * Math.PI * 1.2 * t); // ~72bpm
            double noise = random.nextGaussian() * 0.05;
            signal[i] = (int) ((heartbeat + noise + 1) * 2048); // 12-bit ADC
        }
        return signal;
    }

    /**
     * 전류 센서 데이터 생성 (mA)
     */
    public double generateCurrentSensor(boolean abnormal) {
        if (abnormal) {
            return 2000 + random.nextDouble() * 3000; // 비정상 전류
        }
        return 500 + random.nextDouble() * 500; // 정상 전류
    }

    /**
     * 거리 센서 데이터 생성 (cm)
     */
    public int generateDistanceSensor() {
        return 50 + random.nextInt(450); // 50-500cm
    }

    /**
     * PIR 모션 센서 데이터 생성
     */
    public boolean generatePirSensor(boolean motionPresent) {
        if (motionPresent) {
            return random.nextDouble() > 0.05; // 95% 감지 확률
        }
        return random.nextDouble() < 0.02; // 2% 오탐 확률
    }

    // Data classes
    public record AccelerometerData(double x, double y, double z) {
        public double magnitude() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public boolean isFallDetected() {
            // 자유낙하 감지: 총 가속도가 0.5G 미만
            return magnitude() < 0.5;
        }
    }

    public record EnvironmentData(
            double temperature,
            double humidity,
            int co2Ppm,
            boolean smokeDetected,
            double gasLevelPercent
    ) {
        public boolean isHazardous() {
            return temperature > 40 || co2Ppm > 2000 || smokeDetected || gasLevelPercent > 20;
        }
    }
}

-- SafetyHub 초기화 스크립트

-- 데이터베이스 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 테이블 생성은 JPA가 처리하므로 여기서는 기본 설정만

-- 타임존 설정
SET time_zone = '+09:00';

-- 확인
SELECT 'SafetyHub Database Initialized' AS message;

package com.safetyhub.core.gateway;

/**
 * 메시지 변환 예외
 *
 * 프로토콜별 메시지와 MessageEnvelope 간 변환 시 발생하는 예외
 *
 * 발생 상황:
 * - 메시지 형식이 잘못된 경우
 * - 필수 필드가 누락된 경우
 * - 직렬화/역직렬화 실패
 * - 페이로드 크기 초과
 * - 지원하지 않는 메시지 타입
 *
 * 보안 고려사항:
 * - 에러 메시지에 민감정보 노출 금지
 * - 스택 트레이스에 페이로드 내용 포함 금지
 */
public class MessageConversionException extends RuntimeException {

    /**
     * 기본 생성자
     *
     * @param message 에러 메시지
     */
    public MessageConversionException(String message) {
        super(message);
    }

    /**
     * 원인 예외를 포함하는 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public MessageConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 프로토콜 정보를 포함하는 생성자
     *
     * @param protocol 프로토콜 타입
     * @param message 에러 메시지
     */
    public MessageConversionException(Protocol protocol, String message) {
        super(String.format("[%s] %s", protocol, message));
    }

    /**
     * 프로토콜 정보와 원인 예외를 포함하는 생성자
     *
     * @param protocol 프로토콜 타입
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public MessageConversionException(Protocol protocol, String message, Throwable cause) {
        super(String.format("[%s] %s", protocol, message), cause);
    }
}

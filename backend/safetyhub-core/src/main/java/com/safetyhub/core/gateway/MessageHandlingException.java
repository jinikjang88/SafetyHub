package com.safetyhub.core.gateway;

/**
 * 메시지 처리 예외
 *
 * MessageHandler에서 메시지 처리 중 발생하는 예외
 *
 * 발생 상황:
 * - Hot Path 처리 실패 (긴급 정지, 119 신고 등)
 * - Warm Path 처리 실패 (태스크 분배, 알림 등)
 * - Cold Path 처리 실패 (로그 저장, 분석 등)
 *
 * 보안 고려사항:
 * - 에러 메시지에 민감정보 노출 금지
 * - 스택 트레이스에 페이로드 내용 포함 금지
 */
public class MessageHandlingException extends RuntimeException {

    /**
     * 기본 생성자
     *
     * @param message 에러 메시지
     */
    public MessageHandlingException(String message) {
        super(message);
    }

    /**
     * 원인 예외를 포함하는 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public MessageHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Path 정보를 포함하는 생성자
     *
     * @param path Path 이름 (HOT, WARM, COLD)
     * @param message 에러 메시지
     */
    public MessageHandlingException(String path, String message) {
        super(String.format("[%s PATH] %s", path, message));
    }

    /**
     * Path 정보와 원인 예외를 포함하는 생성자
     *
     * @param path Path 이름
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public MessageHandlingException(String path, String message, Throwable cause) {
        super(String.format("[%s PATH] %s", path, message), cause);
    }
}

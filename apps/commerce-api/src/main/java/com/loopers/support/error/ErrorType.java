package com.loopers.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    /** 범용 에러 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다."),
    
    /** PG 결제 관련 에러 */
    PG_PAYMENT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "PG_PAYMENT_TIMEOUT", "PG 결제 요청이 타임아웃되었습니다."),
    PG_PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PG_PAYMENT_FAILED", "PG 결제에 실패했습니다."),
    PG_PAYMENT_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PG_PAYMENT_SYSTEM_ERROR", "PG 시스템 오류가 발생했습니다."),
    PG_PAYMENT_NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "PG_PAYMENT_NETWORK_ERROR", "PG 네트워크 연결에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

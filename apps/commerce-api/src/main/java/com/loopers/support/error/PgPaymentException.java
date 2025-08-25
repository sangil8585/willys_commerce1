package com.loopers.support.error;

import lombok.Getter;

/**
 * PG 결제 관련 예외 클래스
 */
@Getter
public class PgPaymentException extends RuntimeException {
    
    private final ErrorType errorType;
    private final String pgErrorCode;
    private final String pgErrorMessage;
    
    public PgPaymentException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.pgErrorCode = null;
        this.pgErrorMessage = null;
    }
    
    public PgPaymentException(ErrorType errorType, String message, String pgErrorCode, String pgErrorMessage) {
        super(message);
        this.errorType = errorType;
        this.pgErrorCode = pgErrorCode;
        this.pgErrorMessage = pgErrorMessage;
    }
    
    public PgPaymentException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.pgErrorCode = null;
        this.pgErrorMessage = null;
    }
    
    public PgPaymentException(ErrorType errorType, String message, String pgErrorCode, String pgErrorMessage, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.pgErrorCode = pgErrorCode;
        this.pgErrorMessage = pgErrorMessage;
    }
}

package com.loopers.domain.payment;

/**
 * 결제 상태를 나타내는 enum
 */
public enum PaymentStatus {
    
    /**
     * 결제 대기 중
     */
    PENDING("결제 대기 중"),
    
    /**
     * 결제 처리 중
     */
    PROCESSING("결제 처리 중"),
    
    /**
     * 결제 완료
     */
    COMPLETED("결제 완료"),
    
    /**
     * 결제 실패
     */
    FAILED("결제 실패"),
    
    /**
     * 결제 취소
     */
    CANCELLED("결제 취소"),
    
    /**
     * 아카이브됨 (오래된 결제 건)
     */
    ARCHIVED("아카이브됨");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 결제가 진행 중인 상태인지 확인
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }
    
    /**
     * 결제가 최종 완료된 상태인지 확인
     */
    public boolean isFinalized() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
    
    /**
     * 결제가 아카이브 가능한 상태인지 확인
     */
    public boolean isArchivable() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}

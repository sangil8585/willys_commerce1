package com.loopers.event.like;

import com.loopers.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikeKafkaEvent extends BaseEvent {
    
    private Long userId;
    private Long productId;
    private String action; // CREATED, REMOVED
    
    public LikeKafkaEvent(String eventType, Long userId, Long productId, String action) {
        super(eventType, "commerce-api", "1.0");
        this.userId = userId;
        this.productId = productId;
        this.action = action;
    }
    
    // 좋아요 생성 이벤트
    public static LikeKafkaEvent likeCreated(Long userId, Long productId) {
        return new LikeKafkaEvent("LikeCreated", userId, productId, "CREATED");
    }
    
    // 좋아요 삭제 이벤트
    public static LikeKafkaEvent likeRemoved(Long userId, Long productId) {
        return new LikeKafkaEvent("LikeRemoved", userId, productId, "REMOVED");
    }
}

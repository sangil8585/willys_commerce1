package com.loopers.event.like;

import com.loopers.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikeEvent extends BaseEvent {
    
    private Long userId;
    private Long productId;
    private String action; // CREATED, REMOVED
    
    public LikeEvent(String eventType, Long userId, Long productId, String action) {
        super(eventType, "commerce-api", "1.0");
        this.userId = userId;
        this.productId = productId;
        this.action = action;
    }
    
    // 좋아요 생성 이벤트
    public static LikeEvent likeCreated(Long userId, Long productId) {
        return new LikeEvent("LikeCreated", userId, productId, "CREATED");
    }
    
    // 좋아요 삭제 이벤트
    public static LikeEvent likeRemoved(Long userId, Long productId) {
        return new LikeEvent("LikeRemoved", userId, productId, "REMOVED");
    }
}

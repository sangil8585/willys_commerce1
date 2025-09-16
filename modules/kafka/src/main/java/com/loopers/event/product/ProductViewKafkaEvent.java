package com.loopers.event.product;

import com.loopers.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductViewKafkaEvent extends BaseEvent {
    
    private Long productId;
    
    public ProductViewKafkaEvent(String eventType, Long productId) {
        super(eventType, "commerce-api", "1.0");
        this.productId = productId;
    }
    
    // 상품 조회 이벤트
    public static ProductViewKafkaEvent productViewed(Long productId) {
        return new ProductViewKafkaEvent("ProductViewed", productId);
    }
}

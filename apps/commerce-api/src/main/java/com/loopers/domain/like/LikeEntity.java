package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "likes", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
    },
    indexes = {
        @Index(name = "idx_likes_user_id", columnList = "user_id"),
        @Index(name = "idx_likes_product_id", columnList = "product_id")
    }
)
public class LikeEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;

    protected LikeEntity() {}

    public LikeEntity(Long userId, Long productId) {
        super();
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
        this.userId = userId;
        this.productId = productId;
    }

    public static LikeEntity from(LikeCommand.Create command) {
        return new LikeEntity(
                command.userId(),
                command.productId()
        );
    }
}

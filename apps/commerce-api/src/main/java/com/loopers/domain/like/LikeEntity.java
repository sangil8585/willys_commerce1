package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Getter
@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ref_user_id", "ref_product_id"})
})
public class LikeEntity extends BaseEntity {

    private Long userId;
    private Long productId;

    protected LikeEntity() {}

    public LikeEntity(Long userId, Long productId) {
        super();
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
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

package com.loopers.domain.like;

import java.util.Optional;

public interface LikeRepository {
    LikeEntity save(LikeEntity like);

    Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId);
    
    void delete(LikeEntity like);
}

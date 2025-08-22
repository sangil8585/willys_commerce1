package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    LikeEntity save(LikeEntity like);
    Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId);
    void delete(LikeEntity like);
    List<LikeEntity> findByUserId(Long userId);
    
    // 비정규화를 위한 추가 메서드들
    List<LikeEntity> findByProductId(Long productId);
    long countByProductId(Long productId);
    long countByUserId(Long userId);
}

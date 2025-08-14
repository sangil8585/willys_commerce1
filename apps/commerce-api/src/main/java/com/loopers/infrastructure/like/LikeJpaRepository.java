package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeEntity, Long> {
    
    @Query("SELECT l FROM LikeEntity l WHERE l.userId = :userId AND l.productId = :productId")
    Optional<LikeEntity> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    @Query("SELECT l FROM LikeEntity l WHERE l.userId = :userId")
    List<LikeEntity> findByUserId(@Param("userId") Long userId);
}

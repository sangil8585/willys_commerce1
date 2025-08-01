package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public LikeEntity save(LikeEntity like) {
        return likeJpaRepository.save(like);
    }

    @Override
    public Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return likeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    @Override
    public void delete(LikeEntity like) {
        likeJpaRepository.delete(like);
    }
}

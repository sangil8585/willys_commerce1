package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public void delete(LikeEntity like) {
        likeJpaRepository.delete(like);
    }

    @Override
    public List<LikeEntity> findByUserId(Long userId) {
        return likeJpaRepository.findByUserId(userId);
    }

    @Override
    public List<LikeEntity> findByProductId(Long productId) {
        return likeJpaRepository.findByProductId(productId);
    }

    @Override
    public long countByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }

    @Override
    public long countByUserId(Long userId) {
        return likeJpaRepository.countByUserId(userId);
    }
}

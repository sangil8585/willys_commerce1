package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return likeRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    @Transactional
    public LikeEntity createLike(LikeCommand.Create command) {
        // 멱등성을 위해 이미 존재하는지 확인
        return likeRepository.findByUserIdAndProductId(command.userId(), command.productId())
                .orElseGet(() -> {
                    LikeEntity likeEntity = LikeEntity.from(command);
                    return likeRepository.save(likeEntity);
                });
    }
    
    @Transactional
    public void removeLike(Long userId, Long productId) {
        LikeEntity likeEntity = likeRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요를 찾을 수 없습니다."));
        
        likeRepository.delete(likeEntity);
    }

    @Transactional(readOnly = true)
    public List<LikeEntity> findByUserId(Long userId) {
        return likeRepository.findByUserId(userId);
    }
}

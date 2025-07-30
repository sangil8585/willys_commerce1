package com.loopers.domain.like;

import com.loopers.domain.user.UserRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public LikeEntity createLike(LikeCommand.Create command) {
        // 유저가 존재하는지 확인
        if (!userRepository.findById(command.userId()).isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 유저입니다.");
        }

        // 상품이 존재하는지 확인
        if (!productRepository.findById(command.productId()).isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 상품입니다.");
        }

        // 멱등성을 위해 이미 존재하는지 확인
        return likeRepository.findByUserIdAndProductId(command.userId(), command.productId())
                .orElseGet(() -> {
                    LikeEntity likeEntity = LikeEntity.from(command);
                    return likeRepository.save(likeEntity);
                });
    }

    @Transactional(readOnly = true)
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }
}

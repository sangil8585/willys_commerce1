package com.loopers.domain.like;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserRepository;
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
        
        // 유저가 없으면 예외처리
        boolean userExists = userRepository.existsById(command.userId());
        if (!userExists) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        // 상품이 없으면 예외처리
        boolean productExists = productRepository.existsById(command.productId());
        if (!productExists) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }

        // 멱등성을 위해 이미 존재하는지 확인
        return likeRepository.findByUserIdAndProductId(command.userId(), command.productId())
                .orElseGet(() -> {
                    // 좋아요가 새로 생성되는 경우에만 상품의 좋아요 카운트 증가
                    ProductEntity product = productRepository.findById(command.productId())
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
                    product.incrementLikes();
                    productRepository.save(product);
                    
                    LikeEntity likeEntity = LikeEntity.from(command);
                    return likeRepository.save(likeEntity);
                });
    }

    @Transactional(readOnly = true)
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }
}

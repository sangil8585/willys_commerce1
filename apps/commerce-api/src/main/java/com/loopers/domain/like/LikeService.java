package com.loopers.domain.like;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public LikeEntity createLike(LikeCommand.Create command) {
        if (!userService.existsById(command.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        productService.findById(command.productId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));

        // 멱등성을 위해 이미 존재하는지 확인
        return likeRepository.findByUserIdAndProductId(command.userId(), command.productId())
                .orElseGet(() -> {
                    // 좋아요가 새로 생성되는 경우에만 상품의 좋아요 카운트 증가
                    ProductEntity product = productService.findById(command.productId())
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
                    product.incrementLikes();
                    productService.save(product);
                    
                    LikeEntity likeEntity = LikeEntity.from(command);
                    return likeRepository.save(likeEntity);
                });
    }
    
    @Transactional
    public void removeLike(Long userId, Long productId) {
        if (!userService.existsById(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        productService.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
        
        LikeEntity likeEntity = likeRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요를 찾을 수 없습니다."));
        
        likeRepository.delete(likeEntity);
        
        ProductEntity product = productService.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
        product.decrementLikes();
        productService.save(product);
    }
}

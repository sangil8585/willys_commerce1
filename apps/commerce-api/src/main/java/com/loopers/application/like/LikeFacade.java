package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final UserService userService;
    private final ProductService productService;
    private final LikeService likeService;

    @Transactional
    public LikeInfo like(LikeCommand.Create createCommand) {
        if (!userService.existsById(createCommand.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        ProductEntity product = productService.findById(createCommand.productId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));

        // 멱등성을 위해 이미 존재하는지 확인
        boolean isNewLike = !likeService.existsByUserIdAndProductId(createCommand.userId(), createCommand.productId());

        LikeEntity likeEntity = likeService.createLike(createCommand);
        
        // 비정규화된 likes 카운트 업데이트
        if (isNewLike) {
            product.incrementLikes();
            productService.save(product);
        }
        
        return LikeInfo.from(likeEntity);
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        if (!userService.existsById(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        ProductEntity product = productService.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
        
        // 좋아요 삭제
        likeService.removeLike(userId, productId);
        
        // 비정규화된 likes 카운트 업데이트
        product.decrementLikes();
        productService.save(product);
    }

    @Transactional(readOnly = true)
    public List<LikeInfo> getLikedProducts(Long userId) {
        if (!userService.existsById(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        return likeService.findByUserId(userId).stream()
                .map(LikeInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long userId, Long productId) {
        if (!userService.existsById(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        if (!productService.existsById(productId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }
        
        return likeService.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public long getProductLikeCount(Long productId) {
        if (!productService.existsById(productId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }
        
        return likeService.countByProductId(productId);
    }

    @Transactional(readOnly = true)
    public long getUserLikeCount(Long userId) {
        if (!userService.existsById(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        return likeService.countByUserId(userId);
    }
}

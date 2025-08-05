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

        boolean isNewLike = !likeService.existsByUserIdAndProductId(createCommand.userId(), createCommand.productId());

        LikeEntity likeEntity = likeService.createLike(createCommand);
        
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
        
        likeService.removeLike(userId, productId);
        product.decrementLikes();
        productService.save(product);
    }
}

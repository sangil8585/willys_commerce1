package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final UserService userService;
    private final ProductService productService;
    private final LikeService likeService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LikeInfo like(LikeCommand.Create createCommand) {
        if (!userService.existsById(createCommand.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        if (!productService.existsById(createCommand.productId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }

        LikeEvent.Created likeCreatedEvent = LikeEvent.Created.of(
            createCommand.userId(),
            createCommand.productId()
        );
        eventPublisher.publishEvent(likeCreatedEvent);
        
        return new LikeInfo(null, createCommand.userId(), createCommand.productId());
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        if (!userService.existsById(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        
        if (!productService.existsById(productId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }
        
        LikeEvent.Removed likeRemovedEvent = LikeEvent.Removed.of(
            userId,
            productId
        );
        eventPublisher.publishEvent(likeRemovedEvent);
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

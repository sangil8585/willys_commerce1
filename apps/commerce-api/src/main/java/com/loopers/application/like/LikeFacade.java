package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LikeService likeService;

    @Transactional
    public LikeInfo like(LikeCommand.Create createCommand) {
        // 유저가 없으면 예외처리
        if (!userRepository.existsById(createCommand.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        // 상품이 없으면 예외처리
        if (!productRepository.existsById(createCommand.productId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }

        // 예외를 통과하면 저장하고 반환
        LikeEntity likeEntity = likeService.createLike(createCommand);
        return LikeInfo.from(likeEntity);
    }
}

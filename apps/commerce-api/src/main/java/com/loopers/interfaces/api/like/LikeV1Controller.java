package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.domain.like.LikeCommand;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes/products")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @PostMapping("/{productId}")
    @Override
    public ApiResponse<LikeV1Dto.LikeResponse> likeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable("productId") Long productId
    ) {
        LikeCommand.Create command = new LikeCommand.Create(userId, productId);
        var likeInfo = likeFacade.like(command);
        LikeV1Dto.LikeResponse response = LikeV1Dto.LikeResponse.from(likeInfo);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Void> removeLikeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable("productId") Long productId
    ) {
        likeFacade.unlike(userId, productId);
        return ApiResponse.success(null);
    }

    @GetMapping
    @Override
    public ApiResponse<List<LikeV1Dto.LikeResponse>> getLikedProducts(
            @RequestHeader("X-USER-ID") Long userId
    ) {
        var likeInfos = likeFacade.getLikedProducts(userId);
        List<LikeV1Dto.LikeResponse> responses = likeInfos.stream()
                .map(LikeV1Dto.LikeResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }
}

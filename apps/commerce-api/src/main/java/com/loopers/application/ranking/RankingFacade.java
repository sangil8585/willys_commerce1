package com.loopers.application.ranking;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingInfo;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

@Component
@RequiredArgsConstructor
public class RankingFacade {
    private final RankingService rankingService;
    private final ProductService productService;

    public Page<RankingResult> findRankings(RankingCriteria.Search search, Pageable pageable) {
        RankingCommand.Rankings command = new RankingCommand.Rankings(
                search.size(),
                search.page(),
                search.date(),
                search.period()
        );
        
        Page<RankingInfo> rankingInfos = rankingService.getRankings(command);
        
        List<RankingResult> rankingResults = rankingInfos.getContent().stream()
                .map(rankingInfo -> {
                    ProductEntity productEntity = productService.findById(rankingInfo.productId())
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "Can't find product: " + rankingInfo.productId()));
                    return new RankingResult(
                            productEntity.getId(),
                            productEntity.getName(),
                            productEntity.getPrice(),
                            rankingInfo.rank()
                    );
                }).toList();
        
        return new PageImpl<>(rankingResults, pageable, rankingInfos.getTotalElements());
    }
}

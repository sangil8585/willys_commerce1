package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    public Page<RankingInfo> getRankings(RankingCommand.Rankings command) {
        int page = Math.max(command.page(), 0);
        int size = Math.min(Math.max(command.size(), 5), 20);
        int offset = page * size;

        List<Long> rankedProductIds = rankingRepository.getRankedProducts(offset, size, command.date());
        
        List<RankingInfo> rankingInfos = createRankingInfos(rankedProductIds, offset);

        Long totalCount = rankingRepository.getTotalCount(command.date());
        
        return new PageImpl<>(rankingInfos, PageRequest.of(page, size), totalCount);
    }

    private List<RankingInfo> createRankingInfos(List<Long> productIds, int offset) {
        List<RankingInfo> rankingInfos = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Long rank = (long) (offset + i + 1);
            rankingInfos.add(new RankingInfo(productId, rank));
        }
        return rankingInfos;
    }
}

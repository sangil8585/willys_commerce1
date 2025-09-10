package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingJpaRepository rankingJpaRepository;

    @Override
    public List<Long> getRankedProducts(int offset, int size, LocalDate date) {
        Pageable pageable = PageRequest.of(offset / size, size);
        return rankingJpaRepository.findRankedProducts(date, pageable);
    }

    @Override
    public Long getTotalCount(LocalDate date) {
        return rankingJpaRepository.countByDate(date);
    }
}

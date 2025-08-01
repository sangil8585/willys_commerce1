package com.loopers.domain.brand;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public Optional<BrandEntity> find(Long brandId) {
        return brandRepository.find(brandId);
    }

    @Transactional
    public BrandEntity create(String brandName) {
        BrandEntity brand = BrandEntity.of(brandName);
        return brandRepository.save(brand);
    }
}

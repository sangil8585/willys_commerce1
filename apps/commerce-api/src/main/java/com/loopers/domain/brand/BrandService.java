package com.loopers.domain.brand;

import com.loopers.application.brand.BrandInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public Optional<BrandEntity> find(Long brandId) {
        return brandRepository.find(brandId);
    }

    @Transactional(readOnly = true)
    public BrandEntity getBrand(Long brandId) {
        return brandRepository.find(brandId)
                .orElseThrow(() -> new com.loopers.support.error.CoreException(
                        com.loopers.support.error.ErrorType.NOT_FOUND, 
                        "브랜드를 찾을 수 없습니다. brandId: " + brandId));
    }

    @Transactional(readOnly = true)
    public BrandInfo getBrandInfo(Long brandId) {
        BrandEntity brand = getBrand(brandId);
        return new BrandInfo(brand.getId(), brand.getName());
    }

    @Transactional
    public BrandEntity create(String brandName) {
        BrandEntity brand = BrandEntity.of(brandName);
        return brandRepository.save(brand);
    }
}

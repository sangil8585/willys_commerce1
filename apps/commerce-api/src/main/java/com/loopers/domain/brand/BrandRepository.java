package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandRepository {
    BrandEntity save(BrandEntity brand);
    Optional<BrandEntity> find(Long brandId);

}

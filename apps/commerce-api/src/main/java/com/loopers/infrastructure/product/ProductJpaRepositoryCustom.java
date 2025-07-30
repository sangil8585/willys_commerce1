package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductJpaRepositoryCustom {
    Page<ProductEntity> findAll(ProductCriteria criteria, Pageable pageable);
}

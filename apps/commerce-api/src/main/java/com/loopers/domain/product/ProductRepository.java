package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    ProductEntity save(ProductEntity product);

    Page<ProductEntity> find(ProductCriteria criteria, Pageable pageable);

}

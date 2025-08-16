package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    ProductEntity save(ProductEntity product);

    List<ProductEntity> save(List<ProductEntity> products);

    Page<ProductEntity> find(ProductCriteria criteria, Pageable pageable);

    Optional<ProductEntity> findById(Long productId);
    
    boolean existsById(Long productId);
    
    Optional<ProductEntity> findByIdWithLock(Long productId);
    
    Optional<ProductEntity> findByIdWithLockForLikes(Long productId);

    List<ProductEntity> findByIdsWithLock(List<Long> ids);
}

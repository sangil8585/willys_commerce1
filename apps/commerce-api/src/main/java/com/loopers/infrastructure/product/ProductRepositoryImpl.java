package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public ProductEntity save(ProductEntity product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<ProductEntity> save(List<ProductEntity> products) {
        return productJpaRepository.saveAll(products);
    }

    @Override
    public Page<ProductEntity> find(ProductCriteria criteria, Pageable pageable) {
        return productJpaRepository.findAll(criteria, pageable);
    }

    @Override
    public Optional<ProductEntity> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }
    
    @Override
    public Optional<ProductEntity> findByIdWithLock(Long productId) {
        return productJpaRepository.findByIdWithLock(productId);
    }
    
    @Override
    public Optional<ProductEntity> findByIdWithLockForLikes(Long productId) {
        return productJpaRepository.findByIdWithLockForLikes(productId);
    }

    @Override
    public List<ProductEntity> findByIdsWithLock(List<Long> ids) {
        return productJpaRepository.findByIdsWithLock(ids);
    }
}

package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductEntity createProduct(ProductCommand.Create command) {
        ProductEntity productEntity = ProductEntity.from(command);
        return productRepository.save(productEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> findProducts(ProductCriteria criteria, Pageable pageable) {
        return productRepository.find(criteria, pageable);
    }
}

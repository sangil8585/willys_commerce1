package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    @Transactional(readOnly = true)
    public Optional<ProductEntity> findById(Long productId) {
        return productRepository.findById(productId);
    }
    
    @Transactional
    public ProductEntity save(ProductEntity product) {
        return productRepository.save(product);
    }

    @Transactional
    public void validateAndDeductStock(Long productId, Integer quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        
        if (product.getStock() < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        
        product.deductStock(quantity);
        productRepository.save(product);
    }
}

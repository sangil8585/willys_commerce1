package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


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
    
    @Transactional(readOnly = true)
    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }
    
    @Transactional
    public ProductEntity save(ProductEntity product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deductStock(Map<Long, Integer> itemQuantityMap) {
        // 비관적 락으로 상품 조회
        List<Long> ids = itemQuantityMap.keySet().stream().toList();
        List<ProductEntity> products = productRepository.findByIdsWithLock(ids);
        if(ids.size() != products.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾지 못했습니다");
        }

        for(ProductEntity product : products) {
            product.deductStock(itemQuantityMap.get(product.getId()));
        }

        productRepository.save(products);
    }
    
    @Transactional
    public Optional<ProductEntity> findByIdWithLockForLikes(Long productId) {
        return productRepository.findByIdWithLockForLikes(productId);
    }
}

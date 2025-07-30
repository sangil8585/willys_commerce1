package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;

    @Transactional
    public ProductInfo createProduct(ProductCommand.Create command) {
        // 원래는 여기서 브랜드 조회하고 있으면 생성 없으면 예외 반환 해야함
        ProductEntity productEntity = productService.createProduct(command);
        return ProductInfo.from(productEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProductInfo> findProducts(ProductCriteria criteria, Pageable pageable) {
        // 원래는 브랜드 조회 후 브랜드가 있고 제품을 조회함
        Page<ProductEntity> productEntities = productService.findProducts(criteria, pageable);
        return productEntities.map(ProductInfo::from);
    }
}

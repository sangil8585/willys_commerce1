package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;

    @Transactional
    public ProductInfo createProduct(ProductCommand.Create command) {
        if (command.name() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 필수입니다.");
        }
        if (command.brandId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 필수입니다.");
        }
        if (command.price() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 필수입니다.");
        }
        if (command.stock() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 필수입니다.");
        }
        
        // 브랜드 존재 여부 확인
        String brandName = brandService.find(command.brandId())
                .map(brand -> brand.getName())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다."));
        
        ProductEntity productEntity = productService.createProduct(command);
        return ProductInfo.from(productEntity, brandName);
    }

    @Transactional(readOnly = true)
    public Page<ProductInfo> findProducts(ProductCriteria criteria, Pageable pageable) {
        Page<ProductEntity> productEntities = productService.findProducts(criteria, pageable);
        
        // 브랜드 정보를 조합하여 ProductInfo 생성
        return productEntities.map(productEntity -> {
            String brandName = brandService.find(productEntity.getBrandId())
                    .map(brand -> brand.getName())
                    .orElse("알 수 없는 브랜드");
            return ProductInfo.from(productEntity, brandName);
        });
    }




    
    @Transactional(readOnly = true)
    public ProductInfo findProductById(Long productId) {
        ProductEntity productEntity = productService.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
        
        String brandName = brandService.find(productEntity.getBrandId())
                .map(brand -> brand.getName())
                .orElse("알 수 없는 브랜드");
        
        return ProductInfo.from(productEntity, brandName);
    }
}

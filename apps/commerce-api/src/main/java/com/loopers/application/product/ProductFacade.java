package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        
        // 상품 생성 후 관련 캐시 무효화
        evictProductListCache();
        
        return ProductInfo.from(productEntity, brandName);
    }

    @Transactional(readOnly = true)
    @Cacheable(
        value = "productList", 
        key = "#criteria.hashCode() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize", 
        unless = "#result == null"
    )
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
    @Cacheable(
        value = "product", 
        key = "#productId", 
        unless = "#result == null"
    )
    public ProductInfo findProductById(Long productId) {
        ProductEntity productEntity = productService.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
        
        String brandName = brandService.find(productEntity.getBrandId())
                .map(brand -> brand.getName())
                .orElse("알 수 없는 브랜드");
        
        return ProductInfo.from(productEntity, brandName);
    }

    /**
     * 상품 목록 캐시 무효화 (모든 상품 목록 캐시 삭제)
     */
    @CacheEvict(value = "productList", allEntries = true)
    public void evictProductListCache() {
        // 상품 목록 캐시 무효화
    }

    /**
     * 특정 상품 캐시 무효화
     */
    @CacheEvict(value = "product", key = "#productId")
    public void evictProductCache(Long productId) {
        // 상품 상세 캐시 무효화
    }

    /**
     * 좋아요 수 변경 시 상품 캐시 무효화
     */
    @CacheEvict(value = "product", key = "#productId")
    public void evictProductCacheForLikes(Long productId) {
        // 좋아요 수 변경으로 인한 상품 캐시 무효화
    }
}

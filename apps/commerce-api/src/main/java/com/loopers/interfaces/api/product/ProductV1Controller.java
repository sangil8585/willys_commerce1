package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductFacade productFacade;

    @GetMapping
    @Override
    public ApiResponse<ProductV1Dto.V1.GetProductListResponse> getProductList(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        ProductCriteria criteria = switch (sort) {
            case "price_asc" -> ProductCriteria.orderByPrice(true);
            case "price_desc" -> ProductCriteria.orderByPrice(false);
            case "likes" -> ProductCriteria.orderByLikeCount();
            case "latest" -> ProductCriteria.orderByCreatedAt(false);
            default -> ProductCriteria.orderByCreatedAt(false);
        };

        if (brandId != null) {
            // 기존 정렬 조건을 유지하면서 브랜드 ID 조건 추가
            var existingCriteria = criteria.criteria();
            var newCriteria = new java.util.ArrayList<>(existingCriteria);
            newCriteria.add(new com.loopers.domain.product.ProductCriteria.BrandIdEquals(brandId));
            criteria = new ProductCriteria(newCriteria);
        }

        var productInfoPage = productFacade.findProducts(criteria, pageable);
        ProductV1Dto.V1.GetProductListResponse response = ProductV1Dto.V1.GetProductListResponse.from(productInfoPage);

        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.V1.ProductResponse> getProduct(@PathVariable Long productId) {
        var productInfo = productFacade.findProductById(productId);
        ProductV1Dto.V1.ProductResponse response = ProductV1Dto.V1.ProductResponse.from(productInfo);
        return ApiResponse.success(response);
    }
}

package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductV1Dto {
    public record V1() {
        public record ProductResponse(
                Long id,
                String name,
                Long brandId,
                String brandName,
                Long price,
                Long stock,
                Long likes
        ) {
            public static ProductResponse from(ProductInfo info) {
                return new ProductResponse(
                        info.id(),
                        info.name(),
                        info.brandId(),
                        info.brandName(),
                        info.price(),
                        info.stock(),
                        info.likes()
                );
            }
        }

        public record GetProductListResponse(List<ProductResponse> products, Long totalElements, Integer totalPages) {

            public static GetProductListResponse from(Page<ProductInfo> productInfoPage) {
                List<ProductResponse> productResponses = productInfoPage.getContent().stream()
                        .map(ProductResponse::from)
                        .toList();
                
                return new GetProductListResponse(
                        productResponses,
                        productInfoPage.getTotalElements(),
                        productInfoPage.getTotalPages()
                );
            }
        }
    }
}

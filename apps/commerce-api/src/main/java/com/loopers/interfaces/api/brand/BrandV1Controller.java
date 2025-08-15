package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandV1Controller implements BrandV1ApiSpec {

    private final BrandService brandService;

    @GetMapping("/{brandId}")
    public ApiResponse<BrandV1Dto.V1.BrandResponse> getBrand(@PathVariable Long brandId) {
        var brandInfo = brandService.getBrandInfo(brandId);
        return ApiResponse.success(BrandV1Dto.V1.BrandResponse.from(brandInfo));
    }
} 

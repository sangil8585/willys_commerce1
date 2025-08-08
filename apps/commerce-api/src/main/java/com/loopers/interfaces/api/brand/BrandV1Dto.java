package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;
import com.loopers.domain.brand.BrandCommand;
import jakarta.validation.constraints.NotNull;

public class BrandV1Dto {
    public record V1() {
        public record CreateBrandRequest(
                @NotNull
                String name
        ) {
            public BrandCommand.Create toCommand() {
                return BrandCommand.Create.of(
                        name
                );
            }
        }

        public record BrandResponse(
                Long id,
                String name
        ) {
            public static BrandV1Dto.V1.BrandResponse from(BrandInfo info) {
                return new BrandV1Dto.V1.BrandResponse(
                        info.id(),
                        info.name()
                );
            }
        }
    }



}

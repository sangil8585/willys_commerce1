package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, Long> {

}

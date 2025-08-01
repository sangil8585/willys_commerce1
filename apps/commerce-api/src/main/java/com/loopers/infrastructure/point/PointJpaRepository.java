package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {
    
    Optional<PointEntity> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
} 
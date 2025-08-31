package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Version
    @Column(name = "version")
    private Long version;

    public PointEntity(Long userId, Long amount) {
        this.userId = userId;
        this.amount = amount;
        this.version = 0L;
    }

    public PointEntity(Long id, Long userId, Long amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.version = 0L;
    }

    public void charge(Long chargeAmount) {
        this.amount += chargeAmount;
    }

    public static PointEntity create(Long userId) {
        return new PointEntity(userId, 0L);
    }
} 
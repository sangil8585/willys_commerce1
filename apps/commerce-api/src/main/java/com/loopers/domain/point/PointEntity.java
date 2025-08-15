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
    private String userId;
    
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Version
    @Column(name = "version")
    private Long version;

    public PointEntity(String userId, Long amount) {
        this.userId = userId;
        this.amount = amount;
        this.version = 0L;
    }

    public PointEntity(Long id, String userId, Long amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.version = 0L;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getVersion() {
        return version;
    }

    public void charge(Long chargeAmount) {
        this.amount += chargeAmount;
    }

    public static PointEntity create(String userId) {
        return new PointEntity(userId, 0L);
    }
} 
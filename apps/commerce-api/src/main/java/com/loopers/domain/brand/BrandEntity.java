package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "brand")
public class BrandEntity extends BaseEntity {
    private String name;

    protected BrandEntity() {}

    public BrandEntity(String name) {
        validateName(name);
        this.name = name;
    }

    public static BrandEntity of(String name) {
        return new BrandEntity(name);
    }

    private void validateName(String name) {
        if (name == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드명은 null일 수 없습니다.");
        }
        if (name.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드명은 비어있을 수 없습니다.");
        }
        if (name.length() > 30) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드명은 30자를 초과할 수 없습니다.");
        }
    }

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }


}

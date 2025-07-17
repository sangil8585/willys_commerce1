package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class UserPointVO {
    private Long amount;

    protected UserPointVO() {
        amount = 0L;
    }

    public UserPointVO charge(long amount) {
        UserPointVO charged = new UserPointVO();

        if(amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        charged.amount = this.amount + amount;

        return charged;
    }
}

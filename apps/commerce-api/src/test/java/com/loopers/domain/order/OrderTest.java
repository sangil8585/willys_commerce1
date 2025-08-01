package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;


public class OrderTest {

    @DisplayName("주문 생성")
    @Nested
    class Create {
        @DisplayName("유효한 정보로 주문을 생성한다")
        @Test
        void 주문을_생성한다() {
            // given
            List<OrderCommand.OrderItem> items = List.of(
                    OrderCommand.OrderItem.of(1L, 2, 1000L),
                    OrderCommand.OrderItem.of(2L, 1, 1000L)
            );
            OrderCommand.Create command = new OrderCommand.Create(1L, items);

            // when
            OrderEntity order = OrderEntity.from(command);

            // then
            assertThat(order.getUserId()).isEqualTo(1L);
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotalAmount()).isEqualTo(3000L); // 2개 * 1000 + 1개 * 1000
        }

        @DisplayName("주문 아이템이 없으면 주문 생성에 실패한다")
        @Test
        void 주문아이템_없을경우_생성실패() {
            // given
            OrderCommand.Create command = new OrderCommand.Create(1L, List.of());

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("주문 아이템이 null이면 주문 생성에 실패한다")
        @Test
        void 주문아이템_null일경우_생성실패() {
            // given
            OrderCommand.Create command = new OrderCommand.Create(1L, null);

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("아이템 수량이 0 이하면 주문 생성에 실패한다")
        @Test
        void 아이템수량_0이하일경우_생성실패() {
            // given
            List<OrderCommand.OrderItem> items = List.of(
                    OrderCommand.OrderItem.of(1L, 0, 1000L)
            );
            OrderCommand.Create command = new OrderCommand.Create(1L, items);

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }
}

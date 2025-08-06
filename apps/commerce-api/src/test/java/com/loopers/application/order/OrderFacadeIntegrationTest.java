package com.loopers.application.order;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.brand.BrandService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private PointService pointService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private UserInfo userInfo;
    private ProductEntity testProduct1;
    private ProductEntity testProduct2;

    @BeforeEach
    void setUp() {
        String loginId = "sangil8585";
        String gender = "MALE";
        String birthDate = "1993-02-24";
        String email = "sangil8585@naver.com";

        var userCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        userInfo = userFacade.signUp(userCommand);

        // 포인트 충전
        pointService.charge(userInfo.userId(), 5000L);

        // 테스트용 브랜드 생성
        Long brandId = brandService.create("나이키").getId();

        // 테스트용 상품 생성1
        var productCommand1 = new ProductCommand.Create("티셔츠", brandId, 1000L, 10L, 0L);
        testProduct1 = productService.createProduct(productCommand1);

        // 테스트용 상품 생성2
        var productCommand2 = new ProductCommand.Create("운동복", brandId, 2000L, 5L, 0L);
        testProduct2 = productService.createProduct(productCommand2);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 생성")
    @Nested
    class Order {

        @DisplayName("정상적인 주문을 생성한다")
        @Test
        void 정상적인_주문을_생성한다() {
            // given
            List<OrderCommand.OrderItem> items = List.of(
                OrderCommand.OrderItem.of(testProduct1.getId(), 2, testProduct1.getPrice()),
                OrderCommand.OrderItem.of(testProduct2.getId(), 1, testProduct2.getPrice())
            );
            // 생성
            OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

            // when
            OrderInfo orderInfo = orderFacade.createOrder(command);

            // then
            assertThat(orderInfo.userId()).isEqualTo(userInfo.id());
            assertThat(orderInfo.items()).hasSize(2);
            assertThat(orderInfo.totalAmount()).isEqualTo(4000L); // 2x1000+1x2000

            // 주문 생성 후 포인트 잔액확인
            Optional<Long> remainingPoint = pointService.get(userInfo.userId());
            assertThat(remainingPoint.orElse(null)).isEqualTo(1000L); // 5000-4000
        }

        @DisplayName("재고가 부족하면 주문 생성에 실패한다")
        @Test
        void 재고부족시_주문생성_실패() {
            // given
            List<OrderCommand.OrderItem> items = List.of(
                OrderCommand.OrderItem.of(testProduct1.getId(), 15, testProduct1.getPrice())
            );
            // 생성
            OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(command));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("포인트가 부족하면 주문 생성에 실패한다")
        @Test
        void 포인트부족시_주문생성_실패() {
            // given
            List<OrderCommand.OrderItem> items = List.of(
                OrderCommand.OrderItem.of(testProduct1.getId(), 10, testProduct1.getPrice())
            );
            // 생성
            OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(command));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("존재하지 않는 상품으로 주문시 실패한다")
        @Test
        void 존재하지않는_상품으로_주문시_실패() {
            // given
            List<OrderCommand.OrderItem> items = List.of(
                OrderCommand.OrderItem.of(999L, 1, 1000L)
            );
            // 생성
            OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> orderFacade.createOrder(command));
            assertThat(exception.getMessage()).isEqualTo("상품을 찾을 수 없습니다.");
        }

        @DisplayName("여러 주문을 생성하고 주문 목록을 조회한다")
        @Test
        void 여러주문_생성_및_목록조회() {
            // given
            List<OrderCommand.OrderItem> items1 = List.of(
                OrderCommand.OrderItem.of(testProduct1.getId(), 1, testProduct1.getPrice())
            );
            // 생성
            OrderCommand.Create command1 = OrderCommand.Create.of(userInfo.id(), items1);

            List<OrderCommand.OrderItem> items2 = List.of(
                OrderCommand.OrderItem.of(testProduct2.getId(), 1, testProduct2.getPrice())
            );
            // 생성2
            OrderCommand.Create command2 = OrderCommand.Create.of(userInfo.id(), items2);

            // when
            OrderInfo orderInfo1 = orderFacade.createOrder(command1);
            OrderInfo orderInfo2 = orderFacade.createOrder(command2);

            // then
            assertThat(orderInfo1.totalAmount()).isEqualTo(1000L);
            assertThat(orderInfo2.totalAmount()).isEqualTo(2000L);
            assertThat(orderInfo1.userId()).isEqualTo(userInfo.id());
            assertThat(orderInfo2.userId()).isEqualTo(userInfo.id());

            Optional<Long> finalPoint = pointService.get(userInfo.userId());
            assertThat(finalPoint.orElse(null)).isEqualTo(2000L);
        }

        @DisplayName("주문 후 포인트 잔액이 정확히 차감된다")
        @Test
        void 주문후_포인트잔액_정확히_차감() {
            // given
            Optional<Long> initialPoint = pointService.get(userInfo.userId());
            assertThat(initialPoint.orElse(null)).isEqualTo(5000L);

            List<OrderCommand.OrderItem> items = List.of(
                OrderCommand.OrderItem.of(testProduct1.getId(), 3, testProduct1.getPrice()) // 3000원
            );
            // 생성
            OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

            // when
            OrderInfo orderInfo = orderFacade.createOrder(command);

            // then
            assertThat(orderInfo.totalAmount()).isEqualTo(3000L);
            Optional<Long> remainingPoint = pointService.get(userInfo.userId());
            assertThat(remainingPoint.orElse(null)).isEqualTo(2000L);
        }
    }
} 

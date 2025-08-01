package com.loopers.interfaces.api.point;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMapAdapter;

import java.util.List;
import java.util.Map;

import static com.mysema.commons.lang.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {


    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final UserFacade userFacade;

    @Autowired
    public PointV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp, UserFacade userFacade) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.userFacade = userFacade;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /**
     * - [ ]  포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
     * - [ ]  `X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.
     *
     */
    @DisplayName("포인트 조회")
    @Nested
    class GetPointInfo {
        private static final String ENDPOINT = "/api/v1/points";

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void 포인트조회_성공시_보유_포인트_반환() {
            // given
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );
            var testUser = userFacade.signUp(createCommand);
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(testUser.userId())));

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(null, headers), responseType);

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());

        }

        @DisplayName("`X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void X_USER_ID_헤더가_없을경우_400에러() {
            // given

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(null), responseType);

            System.out.println("스테이터스 : " + response.getStatusCode());
            // then
            assertTrue(response.getStatusCode().is4xxClientError());
        }
    }

    /**
     * - [ ]  존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.
     * - [ ]  존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.
     */
    @DisplayName("포인트 충전")
    @Nested
    class chargePoint {
        private static final String ENDPOINT = "/api/v1/points/charge";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void 천원이상_유저가_충전하면_보유_총량_응답() {
            //given
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );
            var testUser = userFacade.signUp(createCommand);
            PointV1Dto.PointRequest pointRequest = new PointV1Dto.PointRequest(
                    testUser.userId(),
                    1000L
            );

            //when
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(pointRequest), responseType);

            //then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(1000L, response.getBody().data().point());
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void 존재하지_않는_유저_요청시_404응답() {
            // given
            PointV1Dto.PointRequest pointRequest = new PointV1Dto.PointRequest(
                    "sangil",
                    1000L
            );

            // when
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(pointRequest), responseType);


            // that
            assertTrue(response.getStatusCode().is4xxClientError());

        }
    }
}

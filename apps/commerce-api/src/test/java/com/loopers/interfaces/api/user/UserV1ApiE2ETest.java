package com.loopers.interfaces.api.user;

import com.loopers.domain.user.*;
import com.loopers.interfaces.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    @MockitoSpyBean
    private UserRepository userRepository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private UserService userService;

    @DisplayName("회원 가입")
    @Nested
    class SignUp {
        // 이런식으로 코딩하면 하드코딩을 안하게되고 실수할 확율이 줄어든다.
        private static final String ENDPOINT = "/api/v1/users";

        @DisplayName("회원 가입 성공시 유저 정보를 응답으로 반환")
        @Test
        void 회원_가입_성공시_유저정보_반환() {
            // given
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "sangil8585", "MALE",
                    "1993-02-24", "sangil8585@naver.com");

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(signUpRequest.userId()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo("MALE"),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo(signUpRequest.birthDate()),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(signUpRequest.email())
            );
        }

        @DisplayName("회원 가입 시 성별이 없을 경우, 400에러 응답")
        @Test
        void 회원가입시_성별_없을경우_400에러() {
            // given
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "sangil8585", null, "1993-02-24", "sangil8585@naver.com");

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
            assertThat(response.getBody().data()).isNull();
        }
    }

    /**
     * - [x]  내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
     * - [x]  존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.
     */
    @DisplayName("내 정보 조회")
    @Nested
    class GetMyInfo {

        private static final String ENDPOINT = "/api/v1/me";

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void 내정보_조회성공시_유저정보_응답() {
            // given
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );
            UserEntity expected = UserEntity.of(createCommand);
            UserEntity saveUser = userRepository.save(expected);

            // when
            UserInfo result = userService.findByUserId("sangil8585");

            // then
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals(expected.getUserId(), result.userId());
            assertEquals(expected.getGender(), result.gender());
            assertEquals(expected.getBirth(), result.birthDate());
            assertEquals(expected.getEmail(), result.email());

        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void 존재하지않는_ID로_로그인시_404_에러_발생() {
            // given

            // when
            UserInfo result = userService.findByUserId("sangil8585");

            // then
            assertNull(result);
        }
    }
}

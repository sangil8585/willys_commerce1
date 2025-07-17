package com.loopers.interfaces.api.user;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @DisplayName("/")
    @Nested
    class SignUp {
        // 이런식으로 코딩하면 하드코딩을 안하게되고 실수할 확율이 줄어든다.
        private static final String ENDPOINT = "/api/v1/users";

        @DisplayName("회원 가입 성공시 유저 정보를 응답으로 반환")
        @Test
        void 회원_가입_성공시_유저정보_반환() {
            // given
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "sangil8585", "변상일", UserV1Dto.SignUpRequest.GenderRequest.MALE,
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
                    () -> assertThat(response.getBody().data().name()).isEqualTo(signUpRequest.name()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(UserV1Dto.GenderResponse.MALE),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo(signUpRequest.birthDate()),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(signUpRequest.email())
            );
        }

        @DisplayName("회원 가입 시 성별이 없을 경우, 400에러 응답")
        @Test
        void 회원가입시_성별_없을경우_400에러() {
            // given
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "sangil8585", "변상일",null, "1993-02-24", "sangil8585@naver.com");

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
}

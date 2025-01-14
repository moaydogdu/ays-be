package com.ays.user.service.impl;

import com.ays.AbstractUnitTest;
import com.ays.admin_user.model.entity.AdminUserEntity;
import com.ays.admin_user.model.entity.AdminUserEntityBuilder;
import com.ays.admin_user.model.enums.AdminUserStatus;
import com.ays.auth.model.AysIdentity;
import com.ays.auth.model.AysToken;
import com.ays.auth.model.AysTokenBuilder;
import com.ays.auth.model.dto.request.AysLoginRequest;
import com.ays.auth.service.AysInvalidTokenService;
import com.ays.auth.service.AysTokenService;
import com.ays.auth.util.exception.*;
import com.ays.user.model.dto.request.AysUserLoginRequestBuilder;
import com.ays.user.model.entity.UserEntity;
import com.ays.user.model.entity.UserEntityBuilder;
import com.ays.user.model.enums.UserStatus;
import com.ays.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

class UserAuthServiceImplTest extends AbstractUnitTest {

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AysTokenService tokenService;

    @Mock
    private AysInvalidTokenService invalidTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AysIdentity identity;

    @Test
    void givenValidLoginRequest_whenUserAuthenticated_thenReturnAysToken() {
        // Given
        AysLoginRequest mockLoginRequest = new AysUserLoginRequestBuilder().build();

        UserEntity mockUserEntity = new UserEntityBuilder()
                .withStatus(UserStatus.ACTIVE)
                .build();

        // When
        Mockito.when(userRepository.findByUsername(mockLoginRequest.getUsername()))
                .thenReturn(Optional.of(mockUserEntity));

        Mockito.when(passwordEncoder.matches(mockLoginRequest.getPassword(), mockUserEntity.getPassword()))
                .thenReturn(true);

        Mockito.when(tokenService.generate(mockUserEntity.getClaims()))
                .thenReturn(mockUserToken);

        // Then
        AysToken token = userAuthService.authenticate(mockLoginRequest);

        Assertions.assertEquals(mockUserToken, token);

        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockLoginRequest.getUsername());

        Mockito.verify(passwordEncoder, Mockito.times(1))
                .matches(mockLoginRequest.getPassword(), mockUserEntity.getPassword());

        Mockito.verify(tokenService, Mockito.times(1))
                .generate(mockUserEntity.getClaims());
    }

    @Test
    void givenInvalidLoginRequest_whenUserNotFound_thenThrowUsernameNotValidException() {
        // Given
        AysLoginRequest mockLoginRequest = new AysUserLoginRequestBuilder().build();

        // When
        Mockito.when(userRepository.findByUsername(mockLoginRequest.getUsername()))
                .thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(
                UsernameNotValidException.class,
                () -> userAuthService.authenticate(mockLoginRequest)
        );

        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockLoginRequest.getUsername());
    }

    @Test
    void givenInvalidLoginRequest_whenUserNotAuthenticated_thenThrowPasswordNotValidException() {
        // Given
        AysLoginRequest mockLoginRequest = new AysUserLoginRequestBuilder().build();

        UserEntity mockUserEntity = new UserEntityBuilder()
                .withStatus(UserStatus.ACTIVE)
                .build();

        // When
        Mockito.when(userRepository.findByUsername(mockLoginRequest.getUsername()))
                .thenReturn(Optional.of(mockUserEntity));

        Mockito.when(passwordEncoder.matches(mockLoginRequest.getUsername(), mockUserEntity.getPassword()))
                .thenReturn(false);

        // Then
        Assertions.assertThrows(
                PasswordNotValidException.class,
                () -> userAuthService.authenticate(mockLoginRequest)
        );

        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockLoginRequest.getUsername());

        Mockito.verify(passwordEncoder, Mockito.times(1))
                .matches(mockLoginRequest.getPassword(), mockUserEntity.getPassword());
    }

    @Test
    void givenValidLoginRequest_whenUserNotActive_thenThrowUserNotActiveException() {
        // Given
        AysLoginRequest mockLoginRequest = new AysUserLoginRequestBuilder().build();

        UserEntity mockUserEntity = new UserEntityBuilder()
                .withStatus(UserStatus.PASSIVE)
                .build();

        // When
        Mockito.when(userRepository.findByUsername(mockLoginRequest.getUsername()))
                .thenReturn(Optional.of(mockUserEntity));

        // Then
        Assertions.assertThrows(
                UserNotActiveException.class,
                () -> userAuthService.authenticate(mockLoginRequest)
        );

        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockLoginRequest.getUsername());
    }

    @Test
    void givenValidRefreshToken_whenRefreshTokenValidated_thenReturnAysToken() {
        // Given
        String mockRefreshToken = """
                eyJ0eXAiOiJCZWFyZXIiLCJhbGciOiJSUzUxMiJ9.eyJqdGkiOiJjYzU2M2M1OS1lNTRkLTRmZWYtODAzOS1kZDZhZmNmZWM2ZDgiL
                CJpc3MiOiJBWVMiLCJpYXQiOjE2ODU5MDMyMjcsImV4cCI6MTY4NTk4OTYyNywidXNlcm5hbWUiOiIyMzIxODAifQ.ut7tGIFy0T6q
                cbl_qJk5SPqL2AQn6qTvSkhy5Fuh4f8Matg95deWDDcqJELeO9lZQ8Lbvltw7UVS8-1olgWpgO6OBl0Npd6FXNx7s_WEX8RuBVAsBo
                itJlbX6T1sDm-pFO8O_tSTi9JzpJqpFq0ZOBKDBWk9r0ykKngeJzjgx33dA3t-9dv_eUeMqx8C8Ru6ZsTvOuO2BIWQpAhjiAXmDUNN
                8ETrBV1lw1rwOgfeUVqcLlWzCXb7e7BDDIEzrqqf0nO0q8vmjv8k0rMrth7iXhL6UADMcptcKaWq7qRui6mvk_sE1pY5Wqs8M4kQxY
                tFi6OdrUfXYh8jN78vKHf2OQ
                """;

        UserEntity mockUserEntity = new UserEntityBuilder()
                .withStatus(UserStatus.ACTIVE)
                .build();

        Claims mockClaims = AysTokenBuilder.getValidClaims(mockUserEntity.getUsername());

        // When
        Mockito.doNothing().when(tokenService)
                .verifyAndValidate(mockRefreshToken);

        Mockito.when(tokenService.getClaims(mockRefreshToken))
                .thenReturn(mockClaims);

        Mockito.when(userRepository.findByUsername(mockUserEntity.getUsername()))
                .thenReturn(Optional.of(mockUserEntity));

        Mockito.when(tokenService.generate(mockUserEntity.getClaims(), mockRefreshToken))
                .thenReturn(mockUserToken);

        // Then
        AysToken token = userAuthService.refreshAccessToken(mockRefreshToken);

        Assertions.assertEquals(mockUserToken.getAccessToken(), token.getAccessToken());
        Assertions.assertEquals(mockUserToken.getRefreshToken(), token.getRefreshToken());
        Assertions.assertEquals(mockUserToken.getAccessTokenExpiresAt(), token.getAccessTokenExpiresAt());

        Mockito.verify(tokenService, Mockito.times(1))
                .verifyAndValidate(mockRefreshToken);
        Mockito.verify(tokenService, Mockito.times(1))
                .getClaims(mockRefreshToken);
        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockUserEntity.getUsername());
        Mockito.verify(tokenService, Mockito.times(1))
                .generate(mockUserEntity.getClaims(), mockRefreshToken);
    }

    @Test
    void givenInvalidRefreshToken_whenTokenNotVerifiedOrValidate_thenThrowTokenNotValidException() {
        // Given
        String mockRefreshToken = """
                eyJhbGciOiJSUzUxMiJ9.wRhmC0TKAqHmMPb2SlzJBwkKKWwsMH2Tqu3zCCrxUfO1qa4mqTDgNqHIsKYzUQLmMnKhAuKzBx0t
                CN7fkflGAAz1rqWl2oglQqnP3Xx183Zwm8qTo27M6cGFDZYmK9j106J4L9tSGbpQCuvg9pq4QXiyo7pHzWDgsGD2OuQpE4fqVcMq2
                ulZbJkqtYK_H0XSbJN8teYulSot4gAl
                """;

        // When
        Mockito.doThrow(TokenNotValidException.class)
                .when(tokenService).verifyAndValidate(mockRefreshToken);

        // Then
        Assertions.assertThrows(
                TokenNotValidException.class,
                () -> userAuthService.refreshAccessToken(mockRefreshToken)
        );

        Mockito.verify(tokenService, Mockito.times(1))
                .verifyAndValidate(mockRefreshToken);
    }

    @Test
    void givenValidRefreshToken_whenUsernameNotValid_thenThrowUsernameNotValidException() {
        // Given
        String mockRefreshToken = """
                eyJ0eXAiOiJCZWFyZXIiLCJhbGciOiJSUzUxMiJ9.eyJqdGkiOiJjYzU2M2M1OS1lNTRkLTRmZWYtODAzOS1kZDZhZmNmZWM2ZDgiL
                CJpc3MiOiJBWVMiLCJpYXQiOjE2ODU5MDMyMjcsImV4cCI6MTY4NTk4OTYyNywidXNlcm5hbWUiOiIyMzIxODAifQ.ut7tGIFy0T6q
                cbl_qJk5SPqL2AQn6qTvSkhy5Fuh4f8Matg95deWDDcqJELeO9lZQ8Lbvltw7UVS8-1olgWpgO6OBl0Npd6FXNx7s_WEX8RuBVAsBo
                itJlbX6T1sDm-pFO8O_tSTi9JzpJqpFq0ZOBKDBWk9r0ykKngeJzjgx33dA3t-9dv_eUeMqx8C8Ru6ZsTvOuO2BIWQpAhjiAXmDUNN
                8ETrBV1lw1rwOgfeUVqcLlWzCXb7e7BDDIEzrqqf0nO0q8vmjv8k0rMrth7iXhL6UADMcptcKaWq7qRui6mvk_sE1pY5Wqs8M4kQxY
                tFi6OdrUfXYh8jN78vKHf2OQ
                """;

        UserEntity mockUserEntity = new UserEntityBuilder()
                .withStatus(UserStatus.ACTIVE)
                .build();

        Claims mockClaims = AysTokenBuilder.getValidClaims(mockUserEntity.getUsername());

        // When
        Mockito.when(tokenService.getClaims(mockRefreshToken))
                .thenReturn(mockClaims);

        Mockito.when(userRepository.findByUsername(mockUserEntity.getUsername()))
                .thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(
                UsernameNotValidException.class,
                () -> userAuthService.refreshAccessToken(mockRefreshToken)
        );

        Mockito.verify(tokenService, Mockito.times(1))
                .getClaims(mockRefreshToken);
        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockUserEntity.getUsername());
    }

    @Test
    void givenValidRefreshToken_whenUserNotActive_thenThrowUserNotActiveException() {
        // Given
        String mockRefreshToken = """
                eyJ0eXAiOiJCZWFyZXIiLCJhbGciOiJSUzUxMiJ9.eyJqdGkiOiJjYzU2M2M1OS1lNTRkLTRmZWYtODAzOS1kZDZhZmNmZWM2ZDgiL
                CJpc3MiOiJBWVMiLCJpYXQiOjE2ODU5MDMyMjcsImV4cCI6MTY4NTk4OTYyNywidXNlcm5hbWUiOiIyMzIxODAifQ.ut7tGIFy0T6q
                cbl_qJk5SPqL2AQn6qTvSkhy5Fuh4f8Matg95deWDDcqJELeO9lZQ8Lbvltw7UVS8-1olgWpgO6OBl0Npd6FXNx7s_WEX8RuBVAsBo
                itJlbX6T1sDm-pFO8O_tSTi9JzpJqpFq0ZOBKDBWk9r0ykKngeJzjgx33dA3t-9dv_eUeMqx8C8Ru6ZsTvOuO2BIWQpAhjiAXmDUNN
                8ETrBV1lw1rwOgfeUVqcLlWzCXb7e7BDDIEzrqqf0nO0q8vmjv8k0rMrth7iXhL6UADMcptcKaWq7qRui6mvk_sE1pY5Wqs8M4kQxY
                tFi6OdrUfXYh8jN78vKHf2OQ
                """;

        UserEntity mockUserEntity = new UserEntityBuilder()
                .withStatus(UserStatus.PASSIVE).build();

        Claims mockClaims = AysTokenBuilder.getValidClaims(mockUserEntity.getUsername());

        // When
        Mockito.doNothing().when(tokenService)
                .verifyAndValidate(mockRefreshToken);

        Mockito.when(tokenService.getClaims(mockRefreshToken))
                .thenReturn(mockClaims);

        Mockito.when(userRepository.findByUsername(mockUserEntity.getUsername()))
                .thenReturn(Optional.of(mockUserEntity));

        // Then
        Assertions.assertThrows(
                UserNotActiveException.class,
                () -> userAuthService.refreshAccessToken(mockRefreshToken)
        );

        Mockito.verify(tokenService, Mockito.times(1))
                .verifyAndValidate(mockRefreshToken);
        Mockito.verify(tokenService, Mockito.times(1))
                .getClaims(mockRefreshToken);
        Mockito.verify(userRepository, Mockito.times(1))
                .findByUsername(mockUserEntity.getUsername());
    }

    @Test
    void givenValidRefreshToken_whenRefreshTokenAndAccessTokenValidated_thenInvalidateToken() {
        // Given
        AdminUserEntity mockAdminUserEntity = new AdminUserEntityBuilder()
                .withStatus(AdminUserStatus.ACTIVE).build();

        String mockAccessToken = mockUserToken.getAccessToken();
        Claims mockAccessTokenClaims = AysTokenBuilder.getValidClaims(mockAdminUserEntity.getUsername());
        String mockAccessTokenId = mockAccessTokenClaims.getId();

        String mockRefreshToken = mockUserToken.getRefreshToken();
        Claims mockRefreshTokenClaims = AysTokenBuilder.getValidClaims(mockAdminUserEntity.getUsername());
        String mockRefreshTokenId = mockRefreshTokenClaims.getId();

        // When
        Mockito.doNothing().when(tokenService)
                .verifyAndValidate(mockRefreshToken);

        Mockito.when(tokenService.getClaims(mockRefreshToken))
                .thenReturn(mockRefreshTokenClaims);
        Mockito.doNothing().when(invalidTokenService)
                .checkForInvalidityOfToken(mockRefreshTokenId);

        Mockito.when(identity.getAccessToken())
                .thenReturn(mockAccessToken);
        Mockito.when(tokenService.getClaims(mockAccessToken))
                .thenReturn(mockAccessTokenClaims);

        Mockito.doNothing().when(invalidTokenService)
                .invalidateTokens(Set.of(mockAccessTokenId, mockRefreshTokenId));

        // Then
        userAuthService.invalidateTokens(mockRefreshToken);

        Mockito.verify(tokenService, Mockito.times(1))
                .verifyAndValidate(Mockito.anyString());

        Mockito.verify(tokenService, Mockito.times(2))
                .getClaims(Mockito.anyString());
        Mockito.verify(invalidTokenService, Mockito.times(1))
                .checkForInvalidityOfToken(Mockito.anyString());

        Mockito.verify(identity, Mockito.times(1))
                .getAccessToken();

        Mockito.verify(invalidTokenService, Mockito.times(1))
                .invalidateTokens(Mockito.anySet());
    }

    @Test
    void givenInvalidRefreshToken_whenRefreshTokenNotValid_thenThrowTokenNotValidException() {
        // Given
        String mockRefreshToken = "invalid_refresh_token";

        // When
        Mockito.doThrow(TokenNotValidException.class)
                .when(tokenService).verifyAndValidate(mockRefreshToken);

        // Then
        Assertions.assertThrows(
                TokenNotValidException.class,
                () -> userAuthService.invalidateTokens(mockRefreshToken)
        );

        Mockito.verify(tokenService, Mockito.times(1))
                .verifyAndValidate(Mockito.anyString());

        Mockito.verify(tokenService, Mockito.times(0))
                .getClaims(Mockito.anyString());
        Mockito.verify(invalidTokenService, Mockito.times(0))
                .checkForInvalidityOfToken(Mockito.anyString());

        Mockito.verify(identity, Mockito.times(0))
                .getAccessToken();

        Mockito.verify(invalidTokenService, Mockito.times(0))
                .invalidateTokens(Mockito.anySet());
    }

    @Test
    void givenInvalidatedRefreshToken_whenRefreshTokenInvalidated_thenThrowTokenAlreadyInvalidatedException() {
        // Given
        AdminUserEntity mockAdminUserEntity = new AdminUserEntityBuilder()
                .withStatus(AdminUserStatus.ACTIVE).build();

        String mockRefreshToken = mockUserToken.getRefreshToken();
        Claims mockRefreshTokenClaims = AysTokenBuilder.getValidClaims(mockAdminUserEntity.getUsername());
        String mockRefreshTokenId = mockRefreshTokenClaims.getId();

        // When
        Mockito.doNothing().when(tokenService)
                .verifyAndValidate(mockRefreshToken);

        Mockito.when(tokenService.getClaims(mockRefreshToken))
                .thenReturn(mockRefreshTokenClaims);
        Mockito.doThrow(TokenAlreadyInvalidatedException.class)
                .when(invalidTokenService).checkForInvalidityOfToken(mockRefreshTokenId);

        // Then
        Assertions.assertThrows(
                TokenAlreadyInvalidatedException.class,
                () -> userAuthService.invalidateTokens(mockRefreshToken)
        );

        Mockito.verify(tokenService, Mockito.times(1))
                .verifyAndValidate(Mockito.anyString());

        Mockito.verify(tokenService, Mockito.times(1))
                .getClaims(Mockito.anyString());
        Mockito.verify(invalidTokenService, Mockito.times(1))
                .checkForInvalidityOfToken(Mockito.anyString());

        Mockito.verify(identity, Mockito.times(0))
                .getAccessToken();

        Mockito.verify(invalidTokenService, Mockito.times(0))
                .invalidateTokens(Mockito.anySet());
    }

}

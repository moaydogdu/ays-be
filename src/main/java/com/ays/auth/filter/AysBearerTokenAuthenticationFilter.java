package com.ays.auth.filter;

import com.ays.auth.model.AysToken;
import com.ays.auth.model.enums.AysTokenClaims;
import com.ays.auth.service.AysInvalidTokenService;
import com.ays.auth.service.AysTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * AysBearerTokenAuthenticationFilter is a filter that intercepts HTTP requests and processes the Bearer tokens included in the Authorization headers.
 * If the token is valid, the user is authenticated and added to the SecurityContext for the duration of the request.
 * If the token is invalid, a 401 Unauthorized response is returned.
 * <p>The filter uses an instance of AysTokenService to verify and validate the token and retrieve the user authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AysBearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AysTokenService tokenService;
    private final AysInvalidTokenService invalidTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {


        log.debug("API Request was secured with AYS Security!");

        final String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (AysToken.isBearerToken(authorizationHeader)) {
            final String jwt = AysToken.getJwt(authorizationHeader);

            tokenService.verifyAndValidate(jwt);

            final String tokenId = tokenService.getClaims(jwt)
                    .get(AysTokenClaims.JWT_ID.getValue()).toString();
            invalidTokenService.checkForInvalidityOfToken(tokenId);

            final var authentication = tokenService.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}

package pl.bookmarket.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.security.authentication.BearerTokenException;
import pl.bookmarket.security.authentication.JwtService;
import pl.bookmarket.security.filter.AuthorizationFilter;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("${bm.controllers.auth}")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/refresh-token")
    public Map<String, String> refreshAccessToken(@CookieValue(defaultValue = "") String refreshToken,
                                                  @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = "") String authHeader) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BearerTokenException("Refresh token is missing or invalid.");
        }
        if (!authHeader.startsWith(AuthorizationFilter.AUTH_PREFIX)) {
            throw new BearerTokenException("Access token is missing or invalid.");
        }
        String oldAccessToken = authHeader.substring(AuthorizationFilter.AUTH_PREFIX.length());
        Authentication authentication = jwtService.buildAuthentication(oldAccessToken);
        String newAccessToken = jwtService.generateAccessToken(authentication);
        return Collections.singletonMap("accessToken", newAccessToken);
    }
}
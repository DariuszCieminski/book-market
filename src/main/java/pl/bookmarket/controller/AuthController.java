package pl.bookmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.security.authentication.JwtService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final ObjectMapper mapper;

    public AuthController(JwtService jwtService, ObjectMapper mapper) {
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    @PostMapping("/refresh-token")
    public void refreshAccessToken(@CookieValue Cookie refreshToken,
                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                   HttpServletResponse response) throws IOException {
        if (jwtService.validateToken(refreshToken.getValue())) {
            Authentication authentication = jwtService.buildAuthentication(authHeader);
            String newAccessToken = jwtService.generateAccessToken(authentication);
            String responseBody = mapper.writeValueAsString(Collections.singletonMap("accessToken", newAccessToken));
            response.getWriter().write(responseBody);
            response.flushBuffer();
        }
    }
}
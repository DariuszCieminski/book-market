package pl.bookmarket.security.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.security.authentication.JwtService;
import pl.bookmarket.util.AuthUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserDao userDao;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public LoginSuccessHandler(UserDao userDao, JwtService jwtService, ObjectMapper objectMapper) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        String responseBody = prepareLoginResponseBody(authentication, userId);
        Cookie refreshTokenCookie = AuthUtils.getRefreshTokenCookie(jwtService.generateRefreshToken(authentication));
        userDao.updateLastLoginTime(userId, OffsetDateTime.now());
        response.getWriter().write(responseBody);
        response.addCookie(refreshTokenCookie);
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }

    private String prepareLoginResponseBody(Authentication authentication, Long userId) throws JsonProcessingException {
        String[] roles = authentication.getAuthorities().stream()
                                       .map(GrantedAuthority::getAuthority)
                                       .map(authority -> authority.substring("ROLE_".length()))
                                       .toArray(String[]::new);
        LoginResponseDto responseDto = new LoginResponseDto(userId, roles, jwtService.generateAccessToken(authentication));
        return objectMapper.writeValueAsString(responseDto);
    }

    private static class LoginResponseDto {
        private final Long userId;
        private final String[] roles;
        private final String accessToken;

        public LoginResponseDto(Long userId, String[] roles, String accessToken) {
            this.userId = userId;
            this.roles = roles;
            this.accessToken = accessToken;
        }

        public Long getUserId() {
            return userId;
        }

        public String[] getRoles() {
            return roles;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
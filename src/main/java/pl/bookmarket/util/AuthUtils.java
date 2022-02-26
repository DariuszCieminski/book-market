package pl.bookmarket.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.bookmarket.security.authentication.AuthenticatedUser;

import javax.servlet.http.Cookie;
import java.time.Duration;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    public static boolean isAuthenticatedUserId(Long userId) {
        return getAuthenticatedUser().getId().equals(userId);
    }

    public static Cookie getRefreshTokenCookie(String refreshTokenValue) {
        Cookie cookie = new Cookie("refreshToken", refreshTokenValue);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh-token");
        cookie.setMaxAge((int) Duration.ofHours(24).getSeconds());
        return cookie;
    }
}
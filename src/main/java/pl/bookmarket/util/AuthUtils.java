package pl.bookmarket.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.bookmarket.service.authentication.AuthenticatedUser;

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
}
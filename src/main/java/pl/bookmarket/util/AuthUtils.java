package pl.bookmarket.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.Cookie;
import java.time.Duration;
import java.util.function.Predicate;

public final class AuthUtils {

    private AuthUtils() {
    }

    /**
     * Gets user principal from current security context and returns it as provided type or <i>null</i> if the
     * current security context doesn't contain any authentication object.
     *
     * @param principalType type of the Principal stored in {@link Authentication} object
     * @return user principal or <b>null</b> if no authentication exists in the current security context
     * @throws ClassCastException if user principal exists, but cannot be cast to the provided type
     */
    public static <T> T getCurrentUser(Class<T> principalType) throws ClassCastException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return principalType.cast(authentication.getPrincipal());
    }

    /**
     * Makes a boolean check on current authentication principal using given {@link Predicate}. The main purpose
     * of this method is to verify if the user can access a particular object, presumably an entity.
     *
     * <pre>
     *
     * <b>Example 1</b> - Check if current user has a valid name
     *
     * boolean accessGranted = hasAccess(String.class, name -> name.equals("Joe"));
     *
     * <b>Example 2</b> - Check if {@link UserDetails} authentication principal has required role
     *
     * GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CREATOR");
     * boolean accessGranted = hasAccess(UserDetails.class, userDetails -> userDetails.getAuthorities().contains(authority));
     * </pre>
     *
     * @param principalType   type of the Principal stored in {@link Authentication} object
     * @param principalTester a principal predicate
     * @return <i>true</i> if the current principal is not null and the {@link Predicate#test} method of provided
     * predicate returns true, <i>false</i> otherwise
     * @throws ClassCastException if user principal exists, but cannot be cast to the provided type
     */
    public static <T> boolean hasAccess(Class<T> principalType, Predicate<T> principalTester) throws ClassCastException {
        T principal = getCurrentUser(principalType);
        return principal != null && principalTester.test(principal);
    }

    /**
     * Checks if the passed {@link UserDetails} object contains "ROLE_ADMIN" authority.
     *
     * @param user implementation instance of {@link UserDetails} interface, often found being stored as principal in
     *             {@link Authentication} object
     * @return <i>true</i> if the passed object contains "ROLE_ADMIN" authority, <i>false</i> otherwise
     */
    public static boolean isAdmin(UserDetails user) {
        return user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /**
     * Creates JWT refresh token stored in a secure, HTTP-only cookie, valid for 24 hours.
     *
     * @param refreshTokenValue value of the cookie, should contain a valid JWT refresh token
     * @return a {@link Cookie} object containing JWT refresh token
     */
    public static Cookie getRefreshTokenCookie(String refreshTokenValue) {
        Cookie cookie = new Cookie("refreshToken", refreshTokenValue);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh-token");
        cookie.setMaxAge((int) Duration.ofHours(24).getSeconds());
        return cookie;
    }
}
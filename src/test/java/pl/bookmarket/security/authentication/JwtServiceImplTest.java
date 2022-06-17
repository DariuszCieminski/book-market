package pl.bookmarket.security.authentication;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private static final long ACCESS_DURATION = 10;
    private static final long REFRESH_DURATION = 360;
    private static final String DEFAULT_INSTANT = "2022-06-01T10:00:00Z";

    private final Clock clock = Mockito.mock(Clock.class);
    private final io.jsonwebtoken.Clock jwtClock = Mockito.mock(io.jsonwebtoken.Clock.class);
    private final MockedStatic<Clock> mockedClock = Mockito.mockStatic(Clock.class);
    private final JwtServiceImpl jwtService = new JwtServiceImpl(ACCESS_DURATION, REFRESH_DURATION, jwtClock);
    private Instant expectedInstant;

    void setupClock(String instant) {
        mockedClock.when(Clock::systemUTC).thenReturn(clock);
        expectedInstant = Instant.from(Instant.parse(instant));
        Mockito.when(clock.instant()).thenReturn(expectedInstant);
        Mockito.when(jwtClock.now()).thenReturn(Date.from(expectedInstant));
    }

    @AfterEach
    void cleanupClock() {
        mockedClock.close();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGeneratingAccessTokenWithInvalidPrincipal() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("principal", "password");

        assertThrows(IllegalArgumentException.class, () -> jwtService.generateAccessToken(authentication));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGeneratingAccessTokenWithNullAuthentication() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.generateAccessToken(null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGeneratingRefreshTokenWithInvalidPrincipal() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("principal", "password");

        assertThrows(IllegalArgumentException.class, () -> jwtService.generateRefreshToken(authentication));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGeneratingRefreshTokenWithNullAuthentication() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.generateRefreshToken(null));
    }

    @Test
    void shouldSuccessfullyGenerateAccessToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();

        String accessToken = jwtService.generateAccessToken(authentication);

        assertTrue(jwtService.validateToken(accessToken));
        assertEquals("Book Market", jwtService.getClaim(accessToken, Claims.ISSUER, String.class));
        assertEquals(expectedInstant.plus(Duration.ofMinutes(ACCESS_DURATION)).getEpochSecond(),
                     jwtService.getClaim(accessToken, Claims.EXPIRATION, Long.class));
        assertEquals(999, jwtService.getClaim(accessToken, "id", Long.class));
        assertFalse(jwtService.getClaim(accessToken, "isRefresh", Boolean.class));
        String[] roles = jwtService.getClaim(accessToken, "roles", String[].class);
        assertNotNull(roles);
        assertEquals(2, roles.length);
        List<String> rolesList = Arrays.asList(roles);
        assertTrue(rolesList.contains("USER"));
        assertTrue(rolesList.contains("ADMIN"));
    }

    @Test
    void shouldSuccessfullyGenerateRefreshToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();

        String accessToken = jwtService.generateRefreshToken(authentication);

        assertTrue(jwtService.validateToken(accessToken));
        assertEquals("Book Market", jwtService.getClaim(accessToken, Claims.ISSUER, String.class));
        assertEquals(expectedInstant.plus(Duration.ofMinutes(REFRESH_DURATION)).getEpochSecond(),
                     jwtService.getClaim(accessToken, Claims.EXPIRATION, Long.class));
        assertEquals(999, jwtService.getClaim(accessToken, "id", Long.class));
        assertTrue(jwtService.getClaim(accessToken, "isRefresh", Boolean.class));
    }

    @Test
    void shouldSuccessfullyValidateAccessToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String accessToken = jwtService.generateAccessToken(authentication);

        setupClock("2022-06-01T10:10:00Z");
        boolean isTokenValid = jwtService.validateToken(accessToken);

        assertTrue(isTokenValid);
    }

    @Test
    void shouldUnsuccessfullyValidateExpiredAccessToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String accessToken = jwtService.generateAccessToken(authentication);

        setupClock("2022-06-01T10:10:01Z");
        boolean isTokenValid = jwtService.validateToken(accessToken);

        assertFalse(isTokenValid);
    }

    @Test
    void shouldUnsuccessfullyValidateNullToken() {
        boolean isTokenValid = jwtService.validateToken(null);

        assertFalse(isTokenValid);
    }

    @Test
    void shouldSuccessfullyValidateRefreshToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String accessToken = jwtService.generateRefreshToken(authentication);

        setupClock("2022-06-01T16:00:00Z");
        boolean isTokenValid = jwtService.validateToken(accessToken);

        assertTrue(isTokenValid);
    }

    @Test
    void shouldUnsuccessfullyValidateExpiredRefreshToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String accessToken = jwtService.generateRefreshToken(authentication);

        setupClock("2022-06-01T16:00:01Z");
        boolean isTokenValid = jwtService.validateToken(accessToken);

        assertFalse(isTokenValid);
    }

    @Test
    void shouldSuccessfullyGetClaimsFromValidAccessToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String accessToken = jwtService.generateAccessToken(authentication);

        Map<String, Object> tokenClaims = jwtService.getClaims(accessToken);

        assertEquals(5, tokenClaims.size());
        assertTrue(tokenClaims.containsKey(Claims.ISSUER));
        assertTrue(tokenClaims.containsKey(Claims.EXPIRATION));
        assertTrue(tokenClaims.containsKey("id"));
        assertTrue(tokenClaims.containsKey("roles"));
        assertTrue(tokenClaims.containsKey("isRefresh"));
    }

    @Test
    void shouldSuccessfullyGetClaimsFromExpiredRefreshToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String refreshToken = jwtService.generateRefreshToken(authentication);

        setupClock("2022-06-01T17:41:23Z");
        Map<String, Object> tokenClaims = jwtService.getClaims(refreshToken);

        assertEquals(4, tokenClaims.size());
        assertTrue(tokenClaims.containsKey(Claims.ISSUER));
        assertTrue(tokenClaims.containsKey(Claims.EXPIRATION));
        assertTrue(tokenClaims.containsKey("id"));
        assertTrue(tokenClaims.containsKey("isRefresh"));
    }

    @Test
    void shouldThrowBearerTokenExceptionWhenGettingClaimsFromNullToken() {
        assertThrows(BearerTokenException.class, () -> jwtService.getClaims(null));
    }

    @Test
    void shouldReturnValidAuthenticationFromAccessToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String accessToken = jwtService.generateAccessToken(authentication);

        Authentication authFromToken = jwtService.buildAuthentication(accessToken);

        assertNotNull(authFromToken);
        assertTrue(authFromToken instanceof UsernamePasswordAuthenticationToken);
        assertTrue(authFromToken.getPrincipal() instanceof AuthenticatedUser);
        assertEquals("999", authFromToken.getName());
        assertEquals("[PROTECTED]", authFromToken.getCredentials());
        assertEquals(999, ((AuthenticatedUser) authFromToken.getPrincipal()).getId());
        assertEquals(2, authFromToken.getAuthorities().size());
        assertTrue(authFromToken.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authFromToken.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void shouldThrowBearerTokenExceptionWhenBuildingAuthFromRefreshToken() {
        setupClock(DEFAULT_INSTANT);
        Authentication authentication = getAuthentication();
        String refreshToken = jwtService.generateRefreshToken(authentication);

        assertThrows(BearerTokenException.class, () -> jwtService.buildAuthentication(refreshToken));
    }

    private Authentication getAuthentication() {
        AuthenticatedUser user = AuthenticatedUser.builder().id(999L).username("TestUser").password("pass").authorities("USER", "ADMIN").build();
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }
}
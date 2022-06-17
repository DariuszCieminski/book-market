package pl.bookmarket.security.filter;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import pl.bookmarket.security.authentication.BearerTokenException;
import pl.bookmarket.security.authentication.JwtService;
import pl.bookmarket.testhelpers.datafactory.AuthenticationFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig
class AuthorizationFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthorizationFilter authorizationFilter;

    @Test
    void shouldNotSetAuthenticationWhenHavingNoAuthHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authorizationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotSetAuthenticationWhenHavingAuthHeaderStartingWithInvalidPrefix() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "INVALID_PREFIX");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authorizationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotSetAuthenticationWhenHavingAuthHeaderWithExpiredAccessToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, AuthorizationFilter.AUTH_PREFIX + "EXPIRED_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        Mockito.when(jwtService.validateToken(Mockito.anyString())).thenReturn(false);

        authorizationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotSetAuthenticationWhenBuildingAuthenticationThrowsBearerTokenException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, AuthorizationFilter.AUTH_PREFIX + "VALID_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        Mockito.when(jwtService.validateToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtService.buildAuthentication(Mockito.anyString())).thenThrow(BearerTokenException.class);

        authorizationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSuccessfullySetAuthenticationWhenHavingAuthHeaderWithValidAccessToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, AuthorizationFilter.AUTH_PREFIX + "VALID_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        Authentication authentication = AuthenticationFactory.getAuthenticatedUser(1L);
        Mockito.when(jwtService.validateToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtService.buildAuthentication(Mockito.anyString())).thenReturn(authentication);

        authorizationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }
}
package pl.bookmarket.security.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CustomDaoAuthenticationProviderTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomDaoAuthenticationProvider authenticationProvider;

    @Test
    void shouldAuthenticateUserSuccessfully() {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken("test", "test");
        AuthenticatedUser user = AuthenticatedUser.builder().id(1L).username("test").password("test").authorities("USER", "ADMIN").build();
        Mockito.when(userDetailsService.loadUserByUsername("test")).thenReturn(user);
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Authentication authentication = authenticationProvider.authenticate(authenticationToken);

        assertNotNull(authentication);
        assertEquals(user, authentication.getPrincipal());
        assertEquals(user.getPassword(), authentication.getCredentials());
        assertEquals(2, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void shouldReturnNullForUnsupportedAuthentication() {
        Authentication authenticationToken = new TestingAuthenticationToken("test", "test");

        Authentication authentication = authenticationProvider.authenticate(authenticationToken);

        assertNull(authentication);
    }

    @Test
    void shouldThrowBadCredentialsException() {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken("test", "test");
        AuthenticatedUser user = AuthenticatedUser.builder().id(1L).username("test").password("test").authorities("USER").build();
        Mockito.when(userDetailsService.loadUserByUsername("test")).thenReturn(user);
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authenticationProvider.authenticate(authenticationToken));
    }
}
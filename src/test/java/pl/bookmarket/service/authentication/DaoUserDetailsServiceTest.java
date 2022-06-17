package pl.bookmarket.service.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.service.crud.UserService;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DaoUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private DaoUserDetailsService userDetailsService;

    @Test
    void shouldSuccessfullyLoadUserByEmail() {
        User user = UserBuilder.getDefaultUser().build();
        Mockito.when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        assertTrue(userDetails instanceof AuthenticatedUser);
        assertEquals(user.getId(), ((AuthenticatedUser) userDetails).getId());
        assertEquals(user.getLogin(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertNotEquals(user.isBlocked(), userDetails.isEnabled());
        assertEquals(user.getRoles().size(), userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldThrowUserNotFoundException() {
        User user = UserBuilder.getDefaultUser().build();
        user.setEmail("nonexistent@test.com");

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(user.getEmail()));
    }
}
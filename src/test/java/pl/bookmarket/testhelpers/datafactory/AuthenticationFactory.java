package pl.bookmarket.testhelpers.datafactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;

public class AuthenticationFactory {

    public static Authentication getAuthenticatedUser(Long id) {
        AuthenticatedUser user = AuthenticatedUser.builder().id(id).username("TestUser " + id).password("password").authorities("USER").build();
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }

    public static Authentication getAuthenticationFromUser(UserBuilder userBuilder) {
        User user = userBuilder.build();
        String[] roles = user.getRoles().stream().map(Role::getName).toArray(String[]::new);
        AuthenticatedUser principal = AuthenticatedUser.builder()
                                                       .id(user.getId())
                                                       .username(user.getEmail())
                                                       .password(user.getPassword())
                                                       .disabled(user.isBlocked())
                                                       .authorities(roles)
                                                       .build();
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }
}
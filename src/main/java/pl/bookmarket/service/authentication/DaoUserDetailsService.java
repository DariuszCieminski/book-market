package pl.bookmarket.service.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.service.crud.UserService;

@Service
public class DaoUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public DaoUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("The user with following email address does not exist: " + username));

        String[] roles = user.getRoles().stream().map(Role::getName).toArray(String[]::new);

        return AuthenticatedUser.builder()
                                .username(user.getLogin())
                                .password(user.getPassword())
                                .id(user.getId())
                                .authorities(roles)
                                .accountLocked(user.isBlocked())
                                .build();
    }
}
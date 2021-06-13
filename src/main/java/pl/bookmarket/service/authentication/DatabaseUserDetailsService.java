package pl.bookmarket.service.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.core.userdetails.User.builder;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserDao dao;

    @Autowired
    public DatabaseUserDetailsService(UserDao dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = dao.findUserByLogin(username)
                       .orElseThrow(() -> new UsernameNotFoundException("There is no user with login: " + username));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));

        UserBuilder userBuilder = builder();
        return userBuilder.username(username)
                          .password(user.getPassword())
                          .disabled(user.isBlocked())
                          .authorities(authorities)
                          .build();
    }
}
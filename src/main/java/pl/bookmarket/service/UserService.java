package pl.bookmarket.service;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Role;
import pl.bookmarket.model.User;

@Service
public class UserService implements UserDetailsService {

    private final UserDao dao;

    @Autowired
    public UserService(UserDao dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = dao.findUserByLogin(username);

        if (user == null) {
            throw new UsernameNotFoundException("There is no user with login: " + username);
        }

        Set<SimpleGrantedAuthority> authorities = new HashSet<>(user.getRoles().size());

        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        UserBuilder userBuilder = org.springframework.security.core.userdetails.User.builder();

        return userBuilder.username(username)
                          .password(user.getPassword())
                          .disabled(user.isBlocked())
                          .authorities(authorities)
                          .build();
    }
}
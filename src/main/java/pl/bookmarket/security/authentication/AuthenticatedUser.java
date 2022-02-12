package pl.bookmarket.security.authentication;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class AuthenticatedUser implements UserDetails, CredentialsContainer {

    private String password;

    private final String username;

    private final Long id;

    private final Set<GrantedAuthority> authorities;

    private final boolean accountNonExpired;

    private final boolean accountNonLocked;

    private final boolean credentialsNonExpired;

    private final boolean enabled;

    public AuthenticatedUser(String username, String password, Long id, Set<GrantedAuthority> authorities) {
        this(username, password, id, authorities, true, true, true, true);
    }

    public AuthenticatedUser(String username, String password, Long id, Set<GrantedAuthority> authorities, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled) {
        Assert.hasLength(username, "Username must not be empty!");
        Assert.hasLength(password, "Password must not be empty!");
        Assert.notNull(id, "User ID must not be null!");
        Assert.notEmpty(authorities, "Must contain at least 1 authority!");

        this.username = username;
        this.password = password;
        this.id = id;
        this.authorities = Collections.unmodifiableSortedSet(prepareAuthorities(authorities));
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }

    private SortedSet<GrantedAuthority> prepareAuthorities(Set<GrantedAuthority> authorities) {
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(Comparator.comparing(GrantedAuthority::getAuthority));

        authorities.forEach(grantedAuthority -> {
            Assert.isTrue(!grantedAuthority.getAuthority()
                                           .startsWith("ROLE_"), "Authority must not start with ROLE_ prefix!");
            sortedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + grantedAuthority.getAuthority()));
        });

        return sortedAuthorities;
    }

    public static AuthenticatedUserBuilder builder() {
        return new AuthenticatedUserBuilder();
    }

    public static class AuthenticatedUserBuilder {
        private String username;

        private String password;

        private Long id;

        private List<GrantedAuthority> authorities;

        private boolean accountExpired;

        private boolean accountLocked;

        private boolean credentialsExpired;

        private boolean disabled;

        public AuthenticatedUserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AuthenticatedUserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public AuthenticatedUserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuthenticatedUserBuilder authorities(String... authorities) {
            this.authorities = AuthorityUtils.createAuthorityList(authorities);
            return this;
        }

        public AuthenticatedUserBuilder accountExpired(boolean accountExpired) {
            this.accountExpired = accountExpired;
            return this;
        }

        public AuthenticatedUserBuilder accountLocked(boolean accountLocked) {
            this.accountLocked = accountLocked;
            return this;
        }

        public AuthenticatedUserBuilder credentialsExpired(boolean credentialsExpired) {
            this.credentialsExpired = credentialsExpired;
            return this;
        }

        public AuthenticatedUserBuilder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public AuthenticatedUser build() {
            return new AuthenticatedUser(this.username, this.password, this.id, new HashSet<>(this.authorities),
                    !this.accountExpired, !this.accountLocked, !this.credentialsExpired, !this.disabled);
        }
    }
}
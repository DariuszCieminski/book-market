package pl.bookmarket.testhelpers.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pl.bookmarket.security.authentication.AuthenticatedUser;

public class WithAuthenticatedUserSecurityContextFactory implements WithSecurityContextFactory<WithAuthenticatedUser> {

    @Override
    public SecurityContext createSecurityContext(WithAuthenticatedUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .username(annotation.username())
                                                               .password("password")
                                                               .authorities(annotation.roles())
                                                               .id(annotation.id())
                                                               .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(authenticatedUser, authenticatedUser.getPassword(), authenticatedUser.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
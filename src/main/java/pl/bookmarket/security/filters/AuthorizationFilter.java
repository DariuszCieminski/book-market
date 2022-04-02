package pl.bookmarket.security.filters;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.bookmarket.security.authentication.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    public static final String AUTH_PREFIX = "Bearer ";
    private final JwtService jwtService;

    public AuthorizationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        String authorizationToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationToken != null && authorizationToken.startsWith(AUTH_PREFIX)) {
            authorizationToken = authorizationToken.substring(AUTH_PREFIX.length());
            if (jwtService.validateToken(authorizationToken)) {
                Authentication authentication = jwtService.buildAuthentication(authorizationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
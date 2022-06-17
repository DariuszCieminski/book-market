package pl.bookmarket.security.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.bookmarket.security.authentication.BearerTokenException;
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
        String authorizationToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationToken != null && authorizationToken.startsWith(AUTH_PREFIX)) {
            authorizationToken = authorizationToken.substring(AUTH_PREFIX.length());
            if (jwtService.validateToken(authorizationToken)) {
                try {
                    Authentication authentication = jwtService.buildAuthentication(authorizationToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (BearerTokenException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
package pl.bookmarket.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper mapper;

    public AuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super(defaultFilterProcessesUrl, authenticationManager);
        this.mapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (request.getMethod().equals(HttpMethod.POST.name())) {
            LoginDto loginDto = mapper.readValue(request.getReader(), LoginDto.class);
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginDto.username, loginDto.password);
            return getAuthenticationManager().authenticate(authentication);
        } else {
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] {HttpMethod.POST.name()});
        }
    }

    private static class LoginDto {
        String username;
        String password;

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
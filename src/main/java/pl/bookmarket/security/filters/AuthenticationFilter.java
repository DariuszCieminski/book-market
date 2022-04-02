package pl.bookmarket.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper mapper;

    public AuthenticationFilter(@Value("${bm.login-url}") String defaultFilterProcessesUrl, AuthenticationSuccessHandler successHandler,
                                AuthenticationFailureHandler failureHandler, ObjectMapper objectMapper) {
        super(defaultFilterProcessesUrl);
        this.setAuthenticationSuccessHandler(successHandler);
        this.setAuthenticationFailureHandler(failureHandler);
        this.mapper = objectMapper;
    }

    @Autowired
    @Lazy
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
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
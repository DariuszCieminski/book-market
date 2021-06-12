package pl.bookmarket.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.bookmarket.service.UserService;
import pl.bookmarket.validation.handlers.AccessDenied;
import pl.bookmarket.validation.handlers.AuthenticationEntryPointHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/registration", "/authentication",
                        "/reset-password", "/api/users").not().authenticated()
                .antMatchers(HttpMethod.POST, "/change-password", "/change-email").authenticated()
                .antMatchers("/api/users", "/api/genres").hasRole("ADMIN")
                .anyRequest().authenticated().and()
                .exceptionHandling()
                .accessDeniedHandler(new AccessDenied())
                .authenticationEntryPoint(new AuthenticationEntryPointHandler()).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
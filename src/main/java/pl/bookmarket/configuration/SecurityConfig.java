package pl.bookmarket.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.security.filters.AuthenticationFilter;
import pl.bookmarket.security.handlers.AuthenticationEntryPointHandler;
import pl.bookmarket.security.handlers.CustomAccessDeniedHandler;
import pl.bookmarket.security.handlers.LoginFailureHandler;
import pl.bookmarket.security.handlers.LoginSuccessHandler;
import pl.bookmarket.util.SecurityProperties;

import javax.annotation.PostConstruct;
import java.util.Arrays;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private final UserDao userDao;
    private AuthenticationFilter authenticationFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, UserDao userDao) {
        this.authenticationProvider = authenticationProvider;
        this.userDao = userDao;
    }

    @PostConstruct
    private void initAuthenticationFilter() throws Exception {
        this.authenticationFilter = new AuthenticationFilter(props().getLoginUrl(), this.authenticationManager(), objectMapper());
        this.authenticationFilter.setAuthenticationSuccessHandler(new LoginSuccessHandler(userDao));
        this.authenticationFilter.setAuthenticationFailureHandler(new LoginFailureHandler());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure().and()
            .csrf().disable()
            .cors().configurationSource(getCorsConfigurationSource()).and()
            .authorizeRequests()
            .antMatchers(props().getLoginUrl(), props().getErrorControllerUrl()).permitAll()
            .antMatchers(POST, props().getUsersApiUrl()).permitAll()
            .antMatchers(GET, props().getGenresApiUrl()).authenticated()
            .antMatchers(props().getUsersApiUrl(), props().getRolesApiUrl(), props().getGenresApiUrl()).hasRole("ADMIN")
            .anyRequest().authenticated().and()
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .accessDeniedHandler(new CustomAccessDeniedHandler())
            .authenticationEntryPoint(new AuthenticationEntryPointHandler()).and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private CorsConfigurationSource getCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(props().getCorsOrigins());
        configuration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name()));
        configuration.setAllowedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                         .addModule(new JavaTimeModule())
                         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                         .build();
    }

    @Bean
    @ConfigurationProperties
    public SecurityProperties props() {
        return new SecurityProperties();
    }
}
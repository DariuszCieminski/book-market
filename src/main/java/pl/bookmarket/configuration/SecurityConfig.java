package pl.bookmarket.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.bookmarket.security.filters.AuthenticationFilter;
import pl.bookmarket.security.filters.AuthorizationFilter;
import pl.bookmarket.security.handlers.AuthenticationEntryPointHandler;
import pl.bookmarket.security.handlers.CustomAccessDeniedHandler;
import pl.bookmarket.util.SecurityProperties;

import java.util.Arrays;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private final AuthenticationFilter authenticationFilter;
    private final AuthorizationFilter authorizationFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, AuthenticationFilter authenticationFilter, AuthorizationFilter authorizationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.authenticationFilter = authenticationFilter;
        this.authorizationFilter = authorizationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure().and()
            .csrf().disable()
            .cors().configurationSource(getCorsConfigurationSource()).and()
            .authorizeRequests()
            .antMatchers(securityProperties().getLoginUrl(), securityProperties().getErrorControllerUrl()).permitAll()
            .antMatchers(POST, securityProperties().getUsersApiUrl() + "/register").permitAll()
            .anyRequest().authenticated().and()
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(authorizationFilter, AuthenticationFilter.class)
            .exceptionHandling()
            .accessDeniedHandler(new CustomAccessDeniedHandler())
            .authenticationEntryPoint(new AuthenticationEntryPointHandler()).and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private CorsConfigurationSource getCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties().getCorsOrigins());
        configuration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name()));
        configuration.setAllowedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    @ConfigurationProperties
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }
}
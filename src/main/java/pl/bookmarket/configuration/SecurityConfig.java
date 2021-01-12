package pl.bookmarket.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import pl.bookmarket.service.UserService;
import pl.bookmarket.util.CustomPasswordEncoder;
import pl.bookmarket.validation.handlers.AccessDenied;
import pl.bookmarket.validation.handlers.LoginFailureHandler;
import pl.bookmarket.validation.handlers.LoginSuccessHandler;
import pl.bookmarket.validation.handlers.UnauthenticationHandler;

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
        auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("SUPERUSER", "ADMIN", "USER");
        auth.userDetailsService(userService).passwordEncoder(new CustomPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure().and()
            .authorizeRequests()
                .antMatchers("/register", "/login").not().authenticated()
                .antMatchers("/logout", "/books", "/offers", "/market", "/messages", "/api/**").authenticated()
                .antMatchers("/admin/**", "/impersonate", "/switchuser").hasRole("ADMIN")
                .antMatchers("/changepassword").access("isAuthenticated() and !hasRole('SUPERUSER')")
                .antMatchers("/changeemail").access("isAuthenticated() and !hasRole('SUPERUSER')")
                .antMatchers("/resetpassword").access("!isAuthenticated() or hasRole('ADMIN')")
                .antMatchers("/canceluserswitch").hasRole("PREVIOUS_ADMINISTRATOR")
                .anyRequest().permitAll().and()
            .httpBasic().and()
            .formLogin()
                .loginPage("/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .successHandler(new LoginSuccessHandler(userService))
                .failureHandler(new LoginFailureHandler()).and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true).and()
            .exceptionHandling()
                .accessDeniedHandler(new AccessDenied())
                .authenticationEntryPoint(new UnauthenticationHandler()).and()
            .sessionManagement()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login").and()
                .sessionFixation().migrateSession();
    }

    @Bean
    public SwitchUserFilter switchUserFilter() {
        SwitchUserFilter filter = new SwitchUserFilter();
        filter.setUserDetailsService(userService);
        filter.setSwitchUserUrl("/impersonate");
        filter.setFailureHandler(new LoginFailureHandler("/switchuser"));
        filter.setTargetUrl("/");
        filter.setExitUserUrl("/canceluserswitch");
        return filter;
    }
}
package pl.bookmarket.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import pl.bookmarket.service.UserService;
import pl.bookmarket.util.CustomBCryptPasswordEncoder;
import pl.bookmarket.validation.handlers.AccessDenied;
import pl.bookmarket.validation.handlers.LoginFailureHandler;
import pl.bookmarket.validation.handlers.LoginSuccessHandler;
import pl.bookmarket.validation.handlers.AuthenticationEntryPointHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final LoginSuccessHandler loginSuccessHandler;

    @Autowired
    public SecurityConfig(UserService userService, LoginSuccessHandler loginSuccessHandler) {
        this.userService = userService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("admin")
            .password("$2a$10$P5zKKSAMpTfkD9gGrvno/OWPp9lOzLUNEl/nBvxdjWWV.rEqDvuMW")
            .roles("SUPERUSER", "ADMIN", "USER");

        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure().and()
            .authorizeRequests()
                .antMatchers("/register", "/login").not().authenticated()
                .antMatchers("/logout", "/books", "/offers", "/market", "/messages", "/api/**").authenticated()
                .antMatchers("/admin/**", "/impersonate", "/switchuser").hasRole("ADMIN")
                .antMatchers("/changepassword", "/changeemail").access("isAuthenticated() and !hasRole('SUPERUSER')")
                .antMatchers("/resetpassword").access("!isAuthenticated() or hasRole('ADMIN')")
                .antMatchers("/canceluserswitch").hasRole("PREVIOUS_ADMINISTRATOR")
                .anyRequest().permitAll().and()
            .httpBasic().and()
            .formLogin()
                .loginPage("/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureHandler(new LoginFailureHandler()).and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true).and()
            .exceptionHandling()
                .accessDeniedHandler(new AccessDenied())
                .authenticationEntryPoint(new AuthenticationEntryPointHandler()).and()
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new CustomBCryptPasswordEncoder();
    }
}
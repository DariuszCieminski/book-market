package pl.bookmarket.testhelpers.utils;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAuthenticatedUserSecurityContextFactory.class)
public @interface WithAuthenticatedUser {

    String username() default "TestUser";

    String[] roles() default {"USER"};

    long id() default 1L;
}
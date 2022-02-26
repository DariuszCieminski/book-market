package pl.bookmarket.security.authentication;

import org.springframework.security.core.AuthenticationException;

public class BearerTokenException extends AuthenticationException {

    public BearerTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BearerTokenException(String msg) {
        super(msg);
    }
}
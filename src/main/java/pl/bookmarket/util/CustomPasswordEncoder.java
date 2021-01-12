package pl.bookmarket.util;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {

    private static final int PASSWORD_STRENGTH = 10;

    @Override
    public String encode(CharSequence rawPassword) {
        return hash(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

    public static String hash(String text) {
        return BCrypt.hashpw(text, BCrypt.gensalt(PASSWORD_STRENGTH));
    }
}
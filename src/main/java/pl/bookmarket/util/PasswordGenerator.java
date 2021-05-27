package pl.bookmarket.util;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();
    private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private PasswordGenerator() {
    }

    public static String generate() {
        char[] password = new char[PASSWORD_LENGTH];

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password[i] = CHARS[random.nextInt(CHARS.length)];
        }

        return new String(password);
    }
}
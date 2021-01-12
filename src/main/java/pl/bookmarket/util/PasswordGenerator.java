package pl.bookmarket.util;

import java.util.Random;

public class PasswordGenerator {

    private static final int passwordLength = 10;
    private static final char[] characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] password = new char[passwordLength];

    public static String generate() {
        Random random = new Random();

        for (int i = 0; i < passwordLength; i++) {
            password[i] = characters[random.nextInt(characters.length)];
        }

        return new String(password);
    }
}
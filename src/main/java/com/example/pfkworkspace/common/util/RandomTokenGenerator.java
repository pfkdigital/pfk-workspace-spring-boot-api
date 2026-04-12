package com.example.pfkworkspace.common.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomTokenGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateToken() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}

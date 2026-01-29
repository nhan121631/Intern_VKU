package com.vku.job.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private static final Dotenv DOTENV;

    static {
        Dotenv tmp = null;
        try {
            tmp = Dotenv.load();
        } catch (Exception e) {
            // Fallback: if Dotenv cannot be loaded (e.g., in CI), ignore and rely on
            // System.getenv
            tmp = null;
            System.err.println("Warning: .env file not loaded: " + e.getMessage());
        }
        DOTENV = tmp;
    }

    public static String get(String key) {
        if (DOTENV != null) {
            String val = DOTENV.get(key);
            if (val != null)
                return val;
        }
        return System.getenv(key);
    }
}

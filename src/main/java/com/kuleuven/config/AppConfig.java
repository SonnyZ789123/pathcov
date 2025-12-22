package com.kuleuven.config;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is =
                     AppConfig.class
                             .getClassLoader()
                             .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new RuntimeException("application.properties not found on classpath");
            }
            PROPS.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private AppConfig() {}

    public static String get(String key) {
        return System.getProperty(key, PROPS.getProperty(key));
    }
}


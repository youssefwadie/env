package com.github.youssefwadie.env;

import com.github.youssefwadie.env.interfaces.ServerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnvProxyFactoryTest {

    private final static String DB_USERNAME = "youssef";
    private final static String DB_PASSWORD = "youssef";
    private final static Integer SERVER_PORT = 10;
    private final static List<String> ALLOWED_ORIGINS = List.of("http://localhost:4200", "https://example.org");
    private final static List<BigDecimal> ADMIN_IDS = List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3));

    EnvProxyFactory envProxyFactory;

    @BeforeEach
    void setUp() {
        final var env = new HashMap<String, String>();
        env.put("DB_USERNAME", DB_USERNAME);
        env.put("SERVER_PORT", String.valueOf(SERVER_PORT));
        env.put("DB_PASSWORD", DB_PASSWORD);
        env.put("ALLOWED_ORIGINS", String.join(",", ALLOWED_ORIGINS));
        env.put("ADMIN_IDS", String.join(",", ADMIN_IDS.stream().map(String::valueOf).toList()));
        envProxyFactory = new EnvProxyFactory(env);
    }

    @Test
    void createProxy() {
        ServerConfig proxy = envProxyFactory.createProxy(ServerConfig.class);

        Assertions.assertTrue(() -> proxy.getAllowedOrigins().equals(ALLOWED_ORIGINS));
        Assertions.assertTrue(() -> proxy.getUbUsername().equals(DB_USERNAME));
        Assertions.assertTrue(() -> proxy.getDbPassword().equals(DB_PASSWORD));

        Assertions.assertTrue(() -> proxy.getPort().equals(SERVER_PORT));
        Assertions.assertTrue(() -> proxy.getAdminIds().equals(ADMIN_IDS));
    }
}
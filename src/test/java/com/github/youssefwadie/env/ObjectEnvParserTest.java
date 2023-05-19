package com.github.youssefwadie.env;

import com.github.youssefwadie.env.exceptions.NoArgsConstructorNotFoundException;
import com.github.youssefwadie.env.model.AdvancedAppConfig;
import com.github.youssefwadie.env.model.AppConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static com.github.youssefwadie.env.exceptions.NoArgsConstructorNotFoundException.NO_EMPTY_CONSTRUCTOR_FOUND_MSG;

class ObjectEnvParserTest {
    private final static String DB_USERNAME = "youssef";
    private final static String DB_PASSWORD = "youssef";
    private final static Integer SERVER_PORT = 10;
    private final static List<String> ALLOWED_ORIGINS = List.of("http://localhost:4200", "https://example.org");
    private final static List<Integer> ADMIN_IDS = List.of(1, 2, 3);
    private ObjectEnvParser envParser;


    @BeforeEach
    void setUp() {
        final var env = new HashMap<String, String>();
        env.put("DB_USERNAME", DB_USERNAME);
        env.put("SERVER_PORT", String.valueOf(SERVER_PORT));
        env.put("DB_PASSWORD", DB_PASSWORD);
        env.put("ALLOWED_ORIGINS", String.join(",", ALLOWED_ORIGINS));
        env.put("ADMIN_IDS", String.join(",", ADMIN_IDS.stream().map(String::valueOf).toList()));
        envParser = new ObjectEnvParser(env, true);
    }

    @Test
    void parseWithClassType() {
        AppConfig parsedAppConfig = envParser.parse(AppConfig.class);
        Assertions.assertTrue(() -> parsedAppConfig.getAllowedOrigins().equals(ALLOWED_ORIGINS));
        Assertions.assertTrue(() -> parsedAppConfig.getDbPassword().equals(DB_PASSWORD));
        Assertions.assertTrue(() -> parsedAppConfig.getDbUsername().equals(DB_USERNAME));
        Assertions.assertTrue(() -> parsedAppConfig.getPort().equals(SERVER_PORT));
        Assertions.assertTrue(() -> parsedAppConfig.getAdminIds().equals(ADMIN_IDS));
    }

    @Test
    void parseWithInstance() {
        AppConfig appConfig = new AppConfig();
        envParser.parse(appConfig);
        Assertions.assertTrue(() -> appConfig.getAllowedOrigins().equals(ALLOWED_ORIGINS));
        Assertions.assertTrue(() -> appConfig.getDbPassword().equals(DB_PASSWORD));
        Assertions.assertTrue(() -> appConfig.getDbUsername().equals(DB_USERNAME));
        Assertions.assertTrue(() -> appConfig.getPort().equals(SERVER_PORT));
        Assertions.assertTrue(() -> appConfig.getAdminIds().equals(ADMIN_IDS));
    }


    @Test
    void parseWithClass_WhenNoEmptyConstructorFound() {
        RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> envParser.parse(AdvancedAppConfig.class));
        Assertions.assertEquals(runtimeException.getCause().getClass(), NoArgsConstructorNotFoundException.class);
        Assertions.assertEquals(runtimeException.getCause().getMessage(), NO_EMPTY_CONSTRUCTOR_FOUND_MSG);
    }

}
package com.github.youssefwadie.env.interfaces;

import com.github.youssefwadie.env.annotations.Env;

import java.util.List;

public interface ServerConfig {
    @Env("SERVER_PORT")
    Integer getPort();

    @Env("DB_USERNAME")
    String getUbUsername();

    @Env("DB_PASSWORD")
    String getDbPassword();

    @Env(value = "ALLOWED_ORIGINS")
    List<String> getAllowedOrigins();

    @Env(value = "ADMIN_IDS", separator = ",")
    List<? extends Number> getAdminIds();
}

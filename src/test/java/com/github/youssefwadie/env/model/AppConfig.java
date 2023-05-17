package com.github.youssefwadie.env.model;

import com.github.youssefwadie.env.annotations.Env;

import java.util.List;

public class AppConfig {
    @Env("SERVER_PORT")
    private Integer port;

    @Env("DB_USERNAME")
    private String dbUsername;

    @Env("DB_PASSWORD")
    private String dbPassword;

    @Env(value = "ALLOWED_ORIGINS", separator = ",")
    private List<String> allowedOrigins;
    @Env(value = "ADMIN_IDS")
    private List<Integer> adminIds;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<Integer> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(List<Integer> adminIds) {
        this.adminIds = adminIds;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "port=" + port +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", allowedOrigins=" + allowedOrigins +
                '}';
    }
}

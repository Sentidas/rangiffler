package ru.sentidas.rangiffler.config;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

enum DockerConfig implements Config {
    instance;

    private static final String jdbc_prefix = "jdbc:mysql://rangiffler-all-db:3306/";


    @Nonnull
    @Override
    public String frontUrl() {
        return "http://frontend.rangiffler.dc/";
    }

    @Nonnull
    @Override
    public String authUrl() {
        return "http://auth.rangiffler.dc:9001/";
    }

    @Nonnull
    @Override
    public String gatewayUrl() {
        return "http://gateway.rangiffler.dc:8081/";
    }

    @Nonnull
    @Override
    public String userdataUrl() {
        return "http://userdata.rangiffler.dc:8088/";
    }

    @NotNull
    @Override
    public String geoUrl() {
        return "http://geo.rangiffler.dc:8085/";
    }

    @NotNull
    @Override
    public String photoUrl() {
        return "http://photo.rangiffler.dc:8094/";
    }

    @NotNull
    @Override
    public String authJdbcUrl() {
        return jdbc_prefix + "rangiffler-auth";
    }

    @NotNull
    @Override
    public String userdataJdbcUrl() {
        return jdbc_prefix + "rangiffler-userdata";
    }

    @NotNull
    @Override
    public String geoJdbcUrl() {
        return jdbc_prefix + "rangiffler-geo";
    }

    @NotNull
    @Override
    public String photoJdbcUrl() {
        return jdbc_prefix + "rangiffler-photo";
    }

    @Nonnull
    @Override
    public String screenshotBaseDir() {
        return "screenshots/selenoid/";
    }

    @NotNull
    @Override
    public String geoGrpcAddress() {
        return "geo.rangiffler.dc";
    }

    @NotNull
    @Override
    public String photoGrpcAddress() {
        return "photo.rangiffler.dc";
    }

    @NotNull
    @Override
    public String userdataGrpcAddress() {
        return "userdata.rangiffler.dc";
    }

    @NotNull
    @Override
    public String kafkaAddress() {
        return "kafka:9092";
    }

    @Nonnull
    @Override
    public String allureDockerUrl() {
        final String allureDockerApiFromEnv = System.getenv("ALLURE_DOCKER_API");
        return allureDockerApiFromEnv != null
                ? allureDockerApiFromEnv
                : "http://allure:5050/";
    }
}

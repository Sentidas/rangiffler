package ru.sentidas.rangiffler.config;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

enum DockerConfig implements Config {
    instance;

    private static final String jdbc_params =
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8";

    private static final String jdbc_prefix = "jdbc:mysql://rangiffler-all-db:3306/";


    @Nonnull
    @Override
    public String frontUrl() {
        return "http://frontend.rangiffler.dc/";
    }

    @Nonnull
    @Override
    public String authUrl() {
        return "http://auth:9001/";
    }

    @Nonnull
    @Override
    public String gatewayUrl() {
        return "http://gateway:8081/";
    }

    @Nonnull
    @Override
    public String userdataUrl() {
        return "http://userdata:8088/";
    }

    @NotNull
    @Override
    public String geoUrl() {
        return "http://geo:8085/";
    }

    @NotNull
    @Override
    public String photoUrl() {
        return "http://photo:8094/";
    }

    @Override
    public String authJdbcUrl() {
        return jdbc_prefix + "rangiffler-auth" + jdbc_params;
    }

    @NotNull
    @Override
    public String userdataJdbcUrl() {
        return jdbc_prefix + "rangiffler-userdata" + jdbc_params;
    }

    @NotNull
    @Override
    public String geoJdbcUrl() {
        return jdbc_prefix + "rangiffler-geo" + jdbc_params;
    }

    @NotNull
    @Override
    public String photoJdbcUrl() {
        return jdbc_prefix + "rangiffler-photo" + jdbc_params;
    }

    @Nonnull
    @Override
    public String screenshotBaseDir() {
        return "screenshots/selenoid/";
    }

    @NotNull
    @Override
    public String geoGrpcAddress() {
        return "geo";
    }

    @NotNull
    @Override
    public String photoGrpcAddress() {
        return "photo";
    }

    @NotNull
    @Override
    public String userdataGrpcAddress() {
        return "userdata";
    }

    @NotNull
    @Override
    public String kafkaAddress() {
        return "kafka:9092";
    }
}

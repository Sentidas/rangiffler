package ru.sentidas.rangiffler.config;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

enum LocalConfig implements Config {
  instance;

  @Nonnull
  @Override
  public String frontUrl() {
    return "http://127.0.0.1:3001/";
  }

  @Nonnull
  @Override
  public String authUrl() {
    return "http://127.0.0.1:9001/";
  }

  @Override
  public String authJdbcUrl() {
    return "jdbc:mysql://localhost:3306/rangiffler-auth";
  }


  @Nonnull
  @Override
  public String gatewayUrl() {
    return "http://127.0.0.1:8081/";
  }

  @Nonnull
  @Override
  public String userdataUrl() {
    return "http://127.0.0.1:8088/";
  }

  @NotNull
  @Override
  public String userdataJdbcUrl() {
    return "jdbc:mysql://localhost:3306/rangiffler-userdata";
  }

  @NotNull
  @Override
  public String geoUrl() {
    return "http://127.0.0.1:8085/";
  }

  @NotNull
  @Override
  public String geoJdbcUrl() {
    return "jdbc:mysql://localhost:3306/rangiffler-geo";
  }

  @NotNull
  @Override
  public String photoUrl() {
    return "http://127.0.0.1:8094/";
  }

  @NotNull
  @Override
  public String photoJdbcUrl() {
    return "jdbc:mysql://localhost:3306/rangiffler-photo";
  }

  @Nonnull
  @Override
  public String screenshotBaseDir() {
    return "screenshots/local/";
  }

  @NotNull
  @Override
  public String geoGrpcAddress() {
    return "127.0.0.1";
  }

  @NotNull
  @Override
  public String photoGrpcAddress() {
    return "127.0.0.1";
  }

  @NotNull
  @Override
  public String kafkaAddress() {
    return "127.0.0.1:9092";
  }

  @NotNull
  @Override
  public String userdataGrpcAddress() {
    return "127.0.0.1";
  }

  @Nonnull
  @Override
  public String allureDockerUrl() {
    return "http://allure:5050/";
  }
}

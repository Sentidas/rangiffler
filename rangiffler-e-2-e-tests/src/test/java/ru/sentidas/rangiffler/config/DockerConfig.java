package ru.sentidas.rangiffler.config;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

enum DockerConfig implements Config {
  instance;

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
    return "http://userdata.rangiffler.dc:8085/";
  }

  @NotNull
  @Override
  public String photoUrl() {
    return "http://userdata.rangiffler.dc:8094/";
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


}

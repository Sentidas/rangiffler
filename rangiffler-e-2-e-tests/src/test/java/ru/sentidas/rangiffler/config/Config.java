package ru.sentidas.rangiffler.config;

import javax.annotation.Nonnull;

public interface Config {

  static Config getInstance() {
    return "docker".equals(System.getProperty("test.env"))
        ? DockerConfig.instance
        : LocalConfig.instance;
  }

  @Nonnull
  String frontUrl();

  @Nonnull
  String authUrl();

  @Nonnull
  String gatewayUrl();

  @Nonnull
  String userdataUrl();

  @Nonnull
  String geoUrl();

  @Nonnull
  String photoUrl();


  @Nonnull
  String screenshotBaseDir();


  @Nonnull
  String geoGrpcAddress();

  default int geoGrpcPort() {
    return 8086;
  }

  @Nonnull
  String photoGrpcAddress();

  default int photoGrpcPort() {
    return 8095;
  }

  @Nonnull
  String userdataGrpcAddress();

  default int userdataGrpcPort() {
    return 8087;
  }
}

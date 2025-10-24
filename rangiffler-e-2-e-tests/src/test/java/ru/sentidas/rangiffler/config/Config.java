package ru.sentidas.rangiffler.config;

import javax.annotation.Nonnull;
import java.util.List;

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

  String authJdbcUrl();

  @Nonnull
  String gatewayUrl();

  @Nonnull
  String userdataUrl();

  @Nonnull
  String userdataJdbcUrl();

  @Nonnull
  String geoUrl();

  @Nonnull
  String geoJdbcUrl();

  @Nonnull
  String photoUrl();

  @Nonnull
  String photoJdbcUrl();

  @Nonnull
  String screenshotBaseDir();

  @Nonnull
  String geoGrpcAddress();

  default int geoGrpcPort() {
    return 8086;
  }

  @Nonnull
  String photoGrpcAddress();

  @Nonnull
  String kafkaAddress();

  default int photoGrpcPort() {
    return 8095;
  }

  @Nonnull
  String userdataGrpcAddress();

  default int userdataGrpcPort() {
    return 8087;
  }

  default List<String> kafkaTopics() {
    return List.of("rangiffler_user", "rangiffler_photo", "rangiffler.activity");
  }
}

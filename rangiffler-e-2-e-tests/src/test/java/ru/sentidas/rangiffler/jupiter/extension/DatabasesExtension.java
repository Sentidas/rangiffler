package ru.sentidas.rangiffler.jupiter.extension;


import ru.sentidas.rangiffler.data.jdbc.Connections;
import ru.sentidas.rangiffler.data.jpa.EntityManagers;

public class DatabasesExtension implements SuiteExtension {
  @Override
  public void afterSuite() {
    Connections.closeAllConnections();
    EntityManagers.closeAllEmfs();
  }
}

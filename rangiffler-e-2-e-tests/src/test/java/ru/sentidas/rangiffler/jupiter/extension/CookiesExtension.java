package ru.sentidas.rangiffler.jupiter.extension;


import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.sentidas.rangiffler.api.core.ThreadSafeCookieStore;

public class CookiesExtension implements AfterTestExecutionCallback {

  @Override
  public void afterTestExecution(ExtensionContext context) throws Exception {
    ThreadSafeCookieStore.INSTANCE.removeAll();
  }
}

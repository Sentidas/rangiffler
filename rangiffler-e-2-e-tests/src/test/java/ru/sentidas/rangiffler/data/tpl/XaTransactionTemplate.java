package ru.sentidas.rangiffler.data.tpl;

import com.atomikos.icatch.jta.UserTransactionImp;

import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import ru.sentidas.rangiffler.data.jdbc.Connections;
import ru.sentidas.rangiffler.data.jdbc.JdbcConnectionHolders;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
@ParametersAreNonnullByDefault
public class XaTransactionTemplate {

  private final JdbcConnectionHolders holders;
  private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

  private int txTimeoutSeconds = 60;    // опционально, выстави как нужно

  public XaTransactionTemplate(String... jdbcUrl) {
    this.holders = Connections.holders(jdbcUrl);
  }

  public XaTransactionTemplate holdConnectionAfterAction() {
    this.closeAfterAction.set(false);
    return this;
  }

  public @Nullable <T> T execute(Supplier<T>... actions) {
    UserTransaction ut = new UserTransactionImp();
    try {
      ut.setTransactionTimeout(txTimeoutSeconds);
      ut.begin();

      T result = null;
      for (Supplier<T> action : actions) {
        result = action.get();
      }

      ut.commit();
      return result;

    } catch (Exception original) {
      // попытаться откатить, но НИКОГДА не терять первопричину
      try {
        ut.rollback();
      } catch (Exception rollbackEx) {
        original.addSuppressed(rollbackEx); // просто прикрепили
      }
      // пробрасываем именно "original"
      if (original instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(original);
    } finally {
      if (closeAfterAction.get()) {
        holders.close();
      }
    }
  }
}

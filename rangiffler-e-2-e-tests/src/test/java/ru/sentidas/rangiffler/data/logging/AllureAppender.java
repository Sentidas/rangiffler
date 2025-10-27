package ru.sentidas.rangiffler.data.logging;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.StdoutLogger;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.AttachmentProcessor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.apache.commons.lang3.StringUtils.*;

@ParametersAreNonnullByDefault
public class AllureAppender extends StdoutLogger {

  private static final String TEMPLATE = "sql-attachment.ftl";
  private final AttachmentProcessor<AttachmentData> processor = new DefaultAttachmentProcessor();

  @Override
  public void logSQL(int connectionId,
                     String now,
                     long elapsed,
                     Category category,
                     String prepared,
                     String sql,
                     String url) {
    if (StringUtils.isBlank(sql)) return;

    String db = "db";
    try {

      String candidate = substringBetween(url, "3306/", "?");
      if (StringUtils.isBlank(candidate)) {

        candidate = substringAfterLast(url, "/");
      }
      if (StringUtils.isNotBlank(candidate)) {
        db = candidate;
      }
    } catch (Throwable ignored) {

    }

    String title = (sql.split("\\s+")[0].toUpperCase()) + " query to: " + db;

    try {
      SqlAttachmentData data = new SqlAttachmentData(
              title,
              SqlFormatter.of(Dialect.MySql).format(sql)
      );
      processor.addAttachment(data, new FreemarkerAttachmentRenderer(TEMPLATE));
    } catch (Throwable t) {
      org.slf4j.LoggerFactory.getLogger(getClass())
              .warn("Allure SQL attachment failed: {}", t.toString());
    }
  }
}
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

    // Безопасно выясняем "имя БД" только для подписи
    String db = "db";
    try {
      // сначала пытаемся "3306/<db>?..."
      String candidate = substringBetween(url, "3306/", "?");
      if (StringUtils.isBlank(candidate)) {
        // если у URL нет '?', возьмём хвост после последнего '/'
        candidate = substringAfterLast(url, "/");
      }
      if (StringUtils.isNotBlank(candidate)) {
        db = candidate;
      }
    } catch (Throwable ignored) {
      // подпись не критична
    }

    String title = (sql.split("\\s+")[0].toUpperCase()) + " query to: " + db;

    try {
      SqlAttachmentData data = new SqlAttachmentData(
              title,
              SqlFormatter.of(Dialect.MySql).format(sql)
      );
      processor.addAttachment(data, new FreemarkerAttachmentRenderer(TEMPLATE));
    } catch (Throwable t) {
//      // Никогда не валим тест из-за отчётности — логнём в консоль по-старому
//      super.logSQL(connectionId, now, elapsed, category, prepared, sql, url);
      // раньше было: super.logSQL(...); — это и сыпало SQL в консоль
      // делаем тихо, максимум логируем саму ошибку без SQL:
      org.slf4j.LoggerFactory.getLogger(getClass())
              .warn("Allure SQL attachment failed: {}", t.toString());
    }
  }
}
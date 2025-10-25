package ru.sentidas.rangiffler.config.traceId;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Servlet-фильтр, добавляющий в HTTP-ответ заголовок Trace-Id с текущим OpenTelemetry traceId.
 * Ставит заголовок на ранней стадии и повторно после выполнения цепочки, если ранняя попытка
 * не удалась. Нужен корреляции логов и фронтовых ошибок.
 */
@Configuration
public class TraceIdFilterConfig {

    @Bean
    @ConditionalOnClass(name = "io.opentelemetry.api.trace.Span")
    @ConditionalOnProperty(name = "app.trace.response-header.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdResponseFilter> traceIdResponseFilterRegistration() {
        FilterRegistrationBean<TraceIdResponseFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TraceIdResponseFilter());
        reg.addUrlPatterns("/*");
        reg.setName("traceIdResponseFilter");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}

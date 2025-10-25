package ru.sentidas.rangiffler.config.traceId;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet-фильтр, добавляющий в HTTP-ответ заголовок Trace-Id с текущим OpenTelemetry traceId.
 * Ставит заголовок на ранней стадии и повторно после выполнения цепочки, если ранняя попытка
 * не удалась. Нужен корреляции логов и фронтовых ошибок.
 */
public class TraceIdResponseFilter implements Filter {
    private static final String TRACE_HEADER = "Trace-Id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) res;

        // 1) Попытка РАНО
        SpanContext early = Span.current().getSpanContext();
        if (early.isValid()) {
            resp.setHeader(TRACE_HEADER, early.getTraceId());
        }

        try {
            chain.doFilter(req, res);
        } finally {
            // 2) Подстраховка ПОСЛЕ — если не успели/не было спана раньше
            if (!resp.containsHeader(TRACE_HEADER)) {
                SpanContext late = Span.current().getSpanContext();
                if (late.isValid()) {
                    resp.setHeader(TRACE_HEADER, late.getTraceId());
                }
            }
        }
    }
}

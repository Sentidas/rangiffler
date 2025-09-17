package ru.sentidas.rangiffler.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // запускаем РАНО, чтобы успеть до коммита
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

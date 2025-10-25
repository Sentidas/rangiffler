package ru.sentidas.rangiffler.config;

import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

/**
 * WebGraphQlInterceptor, который записывает имя GraphQL-операции (operationName)
 * в активный OpenTelemetry-спан: атрибут "graphql.operation.name".
 * Используется для отладки и мониторинга в jaeger
 */
@Component
public class GraphQlOpNameSpanInterceptor implements WebGraphQlInterceptor {
    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String op = request.getOperationName();
        if (op != null && !op.isBlank()) {
            Span.current().setAttribute("graphql.operation.name", op);
        }
        return chain.next(request);
    }
}

package ru.sentidas.rangiffler.config;

import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

@Component
public class GraphQlOpNameSpanInterceptor implements WebGraphQlInterceptor {
    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String op = request.getOperationName();
        if (op != null && !op.isBlank()) {
            // запишем имя операции в активный спан (корневой HTTP /graphql)
            Span.current().setAttribute("graphql.operation.name", op);
        }
        return chain.next(request);
    }
}

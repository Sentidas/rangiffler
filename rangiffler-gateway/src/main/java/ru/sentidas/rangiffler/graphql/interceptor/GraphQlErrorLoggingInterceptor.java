package ru.sentidas.rangiffler.graphql.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Логирование ошибки GraphQL (errors[]) при HTTP 200.
 */
@Component
public class GraphQlErrorLoggingInterceptor implements WebGraphQlInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GraphQlErrorLoggingInterceptor.class);

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        return chain.next(request).doOnNext(response -> {
            if (!response.getErrors().isEmpty()) {
                response.getErrors().forEach(err -> {
                    Object classification = (err.getExtensions() != null)
                            ? err.getExtensions().get("classification")
                            : null;

                    if (classification == null && err.getErrorType() != null) {
                        classification = err.getErrorType().toString();
                    }
                    log.warn(
                            "GraphQL error: operation='{}', path={}, classification={}, message={}",
                            request.getOperationName(),
                            err.getPath(),
                            classification,
                            err.getMessage()
                    );
                });
            }
        });
    }
}

package ru.sentidas.rangiffler.graphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.InvalidImageFormatException;
import ru.sentidas.rangiffler.error.domain.CountryNotFoundException;
import ru.sentidas.rangiffler.error.graphql.TooManySubQueriesException;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный маппер исключений в GraphQL-ошибки: gRPC → GraphQL ErrorType + extensions.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQlExceptionResolver.class);

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (LOG.isWarnEnabled()) {
            LOG.warn("GraphQL resolver received exception chain:\n{}", formatExceptionChain(ex));
        }

        // Базовые extensions (traceId, operation)
        Map<String, Object> baseExt = new HashMap<>();
        SpanContext ctx = Span.current().getSpanContext();
        if (ctx != null && ctx.isValid()) {
            baseExt.put("traceId", ctx.getTraceId());
        }
        String op = env.getOperationDefinition() != null ? env.getOperationDefinition().getName() : null;
        if (op != null && !op.isBlank()) {
            baseExt.put("operation", op);
        }

        Status grpcStatus = extractGrpcStatus(ex);
        if (grpcStatus != null) {
            Status.Code code = grpcStatus.getCode();
            String description = grpcStatus.getDescription();
            String field = env.getField().getName();          // "photo" | "user" | "friendship" | ...
            String msg = (description != null && !description.isBlank()) ? description : "Service error";

            Map<String, Object> ext = new HashMap<>(baseExt);

            switch (code) {
                case PERMISSION_DENIED -> {
                    ext.put("error", "FORBIDDEN");
                    return GraphqlErrorBuilder.newError(env)
                            .message(msg)
                            .errorType(ErrorType.FORBIDDEN)
                            .extensions(ext)
                            .build();
                }
                case NOT_FOUND -> {
                    ext.put("error", "NOT_FOUND");
                    String notFoundMsg = (description == null || description.isBlank())
                            ? ("photo".equals(field) ? "Photo not found" : "Service error")
                            : description;
                    return GraphqlErrorBuilder.newError(env)
                            .message(notFoundMsg)
                            .errorType(ErrorType.NOT_FOUND)
                            .extensions(ext)
                            .build();
                }
                case INVALID_ARGUMENT -> {
                    ext.put("error", "INVALID_ARGUMENT");
                    return GraphqlErrorBuilder.newError(env)
                            .message(msg)
                            .errorType(ErrorType.BAD_REQUEST)
                            .extensions(ext)
                            .build();
                }
                case FAILED_PRECONDITION -> {
                    ext.put("error", "FAILED_PRECONDITION");
                    return GraphqlErrorBuilder.newError(env)
                            .message(msg)
                            .errorType(ErrorType.BAD_REQUEST)
                            .extensions(ext)
                            .build();
                }
                case ALREADY_EXISTS -> {
                    ext.put("error", "ALREADY_EXISTS");
                    return GraphqlErrorBuilder.newError(env)
                            .message(msg)
                            .errorType(ErrorType.BAD_REQUEST)
                            .extensions(ext)
                            .build();
                }
                case UNAUTHENTICATED -> {
                    ext.put("error", "UNAUTHORIZED");
                    String unauthMsg = (description == null || description.isBlank())
                            ? "Authorization required"
                            : description;
                    return GraphqlErrorBuilder.newError(env)
                            .message(unauthMsg)
                            .errorType(ErrorType.UNAUTHORIZED)
                            .extensions(ext)
                            .build();
                }
                case RESOURCE_EXHAUSTED -> {
                    ext.put("error", "TOO_MANY_REQUESTS");
                    String tooMany = (description == null || description.isBlank())
                            ? "Too many requests"
                            : description;
                    return GraphqlErrorBuilder.newError(env)
                            .message(tooMany)
                            .errorType(ErrorType.FORBIDDEN)
                            .extensions(ext)
                            .build();
                }
                case INTERNAL, UNKNOWN -> {
                    ext.put("error", "INTERNAL");
                    String internalMsg = (description == null || description.isBlank())
                            ? "Unexpected service error"
                            : description;
                    return GraphqlErrorBuilder.newError(env)
                            .message(internalMsg)
                            .errorType(ErrorType.INTERNAL_ERROR)
                            .extensions(ext)
                            .build();
                }
                default -> {
                    ext.put("error", "INTERNAL");
                    return GraphqlErrorBuilder.newError(env)
                            .message(msg)
                            .errorType(ErrorType.INTERNAL_ERROR)
                            .extensions(ext)
                            .build();
                }
            }
        }

        if (ex instanceof InvalidImageFormatException e) {
            Map<String, Object> ext = new HashMap<>(baseExt);
            ext.put("error", "UNSUPPORTED_IMAGE_FORMAT");
            ext.put("allowed", e.getAllowed());
            String msg = "Unsupported image format. Allowed: " + String.join(", ", e.getAllowed());
            return GraphqlErrorBuilder.newError(env)
                    .message(msg)
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(ext)
                    .build();
        }

        if (ex instanceof CountryNotFoundException e) {
            Map<String, Object> ext = new HashMap<>(baseExt);
            ext.put("error", "COUNTRY_NOT_FOUND");
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(ext)
                    .build();
        }

        if (ex instanceof TooManySubQueriesException e) {
            Map<String, Object> ext = new HashMap<>(baseExt);
            ext.put("error", "TOO_MANY_SUB_QUERIES");
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.FORBIDDEN)
                    .extensions(ext)
                    .build();
        }

//        if (ex instanceof AuthenticationException) {
//            Map<String, Object> ext = new HashMap<>(baseExt);
//            ext.put("error", "UNAUTHORIZED");
//            return GraphqlErrorBuilder.newError(env)
//                    .message("Authorization required")
//                    .errorType(ErrorType.UNAUTHORIZED)
//                    .extensions(ext)
//                    .build();
//        }

        if (ex instanceof AccessDeniedException) {
            Map<String, Object> ext = new HashMap<>(baseExt);
            ext.put("error", "FORBIDDEN");
            return GraphqlErrorBuilder.newError(env)
                    .message("Access is denied")
                    .errorType(ErrorType.FORBIDDEN)
                    .extensions(ext)
                    .build();
        }

        Map<String, Object> ext = new HashMap<>(baseExt);
        ext.put("error", "UNEXPECTED_ERROR");
        return GraphqlErrorBuilder.newError(env)
                .message("Unexpected error")
                .errorType(ErrorType.INTERNAL_ERROR)
                .extensions(ext)
                .build();
    }

    // Извлечение gRPC Status из цепочки причин (StatusRuntimeException и StatusException)
    private Status extractGrpcStatus(Throwable ex) {
        Throwable t = ex;
        int depth = 0; // защита от зацикливания
        while (t != null && depth++ < 20) {
            if (t instanceof StatusRuntimeException sre) {
                return sre.getStatus();
            }
            if (t instanceof StatusException se) {
                return se.getStatus();
            }
            t = t.getCause();
        }
        return null;
    }

    // Диагностика: форматирование цепочки причин
    private String formatExceptionChain(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable t = ex;
        int depth = 0;
        while (t != null && depth++ < 20) {
            sb.append("#").append(depth)
                    .append(" => ").append(t.getClass().getName())
                    .append(": ").append(t.getMessage() == null ? "" : t.getMessage())
                    .append("\n");
            t = t.getCause();
        }
        return sb.toString();
    }
}

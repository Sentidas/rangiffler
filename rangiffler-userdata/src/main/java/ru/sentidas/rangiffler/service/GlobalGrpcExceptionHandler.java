package ru.sentidas.rangiffler.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.hibernate.exception.ConstraintViolationException;
import ru.sentidas.rangiffler.InvalidImageFormatException;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.ex.SameUsernameException;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {
    // 404:  "не найдено"
    @GrpcExceptionHandler(NotFoundException.class)
    public StatusRuntimeException handleNotFound(NotFoundException e) {
        log.warn("gRPC NOT_FOUND: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    // 400: неверные аргументы
    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleBadArg(IllegalArgumentException e) {
        log.warn("gRPC INVALID_ARGUMENT: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    // 409: нарушение уникальности
    @GrpcExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public StatusRuntimeException duplicate(ConstraintViolationException e) {
        final String msg = "Username already exists";
        log.warn("ALREADY_EXISTS: {} (cause: {})", msg, e.getSQLException() != null ? e.getSQLException().getMessage() : "n/a");
        return Status.ALREADY_EXISTS
                .withDescription(msg)
                .asRuntimeException();
    }

    @GrpcExceptionHandler(SameUsernameException.class)
    public StatusRuntimeException handleSameUsername(SameUsernameException e) {
        final String msg = (e.getMessage() == null || e.getMessage().isBlank())
                ? "Can't create friendship request for self user"
                : e.getMessage();
        log.warn("gRPC PERMISSION_DENIED: {}", msg);
        return Status.PERMISSION_DENIED
                .withDescription(msg)
                .asRuntimeException();
    }

    // 400: неверный формат изображения
    @GrpcExceptionHandler(InvalidImageFormatException.class)
    public StatusRuntimeException handleInvalidFormat(InvalidImageFormatException e) {
        final String msg = e.getMessage() + " | allowed: " + String.join(", ", e.getAllowed());
        log.warn("gRPC INVALID_ARGUMENT: {}", msg);
        return Status.INVALID_ARGUMENT.withDescription(msg).asRuntimeException();
    }

    @GrpcExceptionHandler(Throwable.class)
    public StatusRuntimeException handleAny(Throwable e) {
        log.error("gRPC INTERNAL (unhandled)", e);
        return Status.INTERNAL.withDescription("Unhandled error").asRuntimeException();
    }
}

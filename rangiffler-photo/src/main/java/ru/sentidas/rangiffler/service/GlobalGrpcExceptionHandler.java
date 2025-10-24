package ru.sentidas.rangiffler.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import ru.sentidas.rangiffler.ex.AccessDeniedException;
import ru.sentidas.rangiffler.ex.NotFoundException;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(NotFoundException.class)
    public StatusRuntimeException handleNotFound(NotFoundException e) {
        log.warn("gRPC NOT_FOUND: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(AccessDeniedException.class)
    public StatusRuntimeException handleAccessDenied(AccessDeniedException e) {
        log.warn("gRPC PERMISSION_DENIED: {}", e.getMessage());
        return Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleBadArg(IllegalArgumentException e) {
        log.warn("gRPC INVALID_ARGUMENT: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    // 503 сетевая/инфраструктурная Minio не доступен
    @GrpcExceptionHandler(ru.sentidas.rangiffler.ex.StorageUnavailableException.class)
    public StatusRuntimeException handleStorageUnavailable(ru.sentidas.rangiffler.ex.StorageUnavailableException e) {
        log.warn("gRPC UNAVAILABLE: {}", e.getMessage());
        return Status.UNAVAILABLE
                .withDescription(e.getMessage())
                .asRuntimeException();
    }

    @GrpcExceptionHandler(Throwable.class)
    public StatusRuntimeException handleAny(Throwable e) {
        log.error("gRPC INTERNAL (unhandled)", e);
        return Status.INTERNAL.withDescription("Unhandled error").asRuntimeException();
    }
}

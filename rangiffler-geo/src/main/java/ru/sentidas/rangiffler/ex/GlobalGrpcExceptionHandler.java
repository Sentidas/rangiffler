package ru.sentidas.rangiffler.ex;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import java.nio.file.AccessDeniedException;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException passThrough(StatusRuntimeException e) {
        log.warn("gRPC passthrough: {}", e.getStatus());
        return e;
    }

    @GrpcExceptionHandler(NotFoundException.class)
    public StatusRuntimeException handleNotFound(NotFoundException e) {
        log.warn("gRPC NOT_FOUND: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleBadArg(IllegalArgumentException e) {
        log.warn("gRPC INVALID_ARGUMENT: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(Throwable.class)
    public StatusRuntimeException handleAny(Throwable e) {
        log.error("gRPC INTERNAL (unhandled)", e);
        return Status.INTERNAL.withDescription("Unhandled error").asRuntimeException();
    }
}

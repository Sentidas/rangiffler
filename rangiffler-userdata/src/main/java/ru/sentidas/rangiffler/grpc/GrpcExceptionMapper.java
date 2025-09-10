package ru.sentidas.rangiffler.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionMapper {

    @GrpcExceptionHandler(ru.sentidas.rangiffler.ex.NotFoundException.class)
    public StatusRuntimeException handleNotFound(Exception e) {
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(ru.sentidas.rangiffler.ex.SameUsernameException.class)
    public StatusRuntimeException handleConflict(Exception e) {
        return Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleBadRequest(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    // на крайний случай — логируем и шлём INTERNAL:
    @GrpcExceptionHandler(Throwable.class)
    public StatusRuntimeException handleAny(Throwable t) {
        // залогируйте t полностью
        return Status.INTERNAL.withDescription("Unexpected error").asRuntimeException();
    }

}

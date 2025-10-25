package ru.sentidas.rangiffler.service.utils;

import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

public final class GrpcCall {
    private GrpcCall() {
    }

    public static <T> T run(Supplier<T> call, String serviceName) {
        try {
            return call.get();
        } catch (StatusRuntimeException e) {
            throw status(serviceName, e);
        }
    }

    public static void runVoid(Runnable call, String serviceName) {
        try {
            call.run();
        } catch (StatusRuntimeException e) {
            throw status(serviceName, e);
        }
    }

    public static ResponseStatusException status(String service, StatusRuntimeException e) {
        var code = e.getStatus().getCode();
        var desc = e.getStatus().getDescription();
        HttpStatus http = switch (code) {
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            case INVALID_ARGUMENT, FAILED_PRECONDITION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        String msg = "gRPC " + service + " failed: " + code + (desc != null ? " - " + desc : "");
        return new ResponseStatusException(http, msg, e);
    }
}

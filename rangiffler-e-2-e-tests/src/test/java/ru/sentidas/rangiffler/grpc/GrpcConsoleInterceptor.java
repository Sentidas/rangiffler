package ru.sentidas.rangiffler.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;

public class GrpcConsoleInterceptor implements io.grpc.ClientInterceptor {

  private static final JsonFormat.Printer printer = JsonFormat.printer();

  // Фича-флаг: детальный трассинг gRPC вручную (дефолт — false).
  // private static final boolean TRACE = Boolean.getBoolean("rangiffler.grpc.trace");
  private static final boolean TRACE = true;
  private static final boolean TRACE_request = false;

  // Новый флаг: управляет печатью в консоль (дефолт — выключено).
  // Включить при необходимости: -Drangiffler.grpc.console=true
  private static final boolean TO_CONSOLE = Boolean.getBoolean("rangiffler.grpc.console");

  // Единая точка вывода: либо консоль, либо TODO для Allure
  private static void traceOut(String label, Object protoMsg) {
    try {
      String json = printer.print((MessageOrBuilder) protoMsg);
      if (TO_CONSOLE) {
        System.out.println(label + " " + json);
      } else {
        // TODO: сделать вывод в Allure:
      }
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
    return new ForwardingClientCall.SimpleForwardingClientCall(
            channel.newCall(methodDescriptor, callOptions)
    ) {

      @Override
      public void sendMessage(Object message) {
        if (TRACE_request) {
          traceOut("REQUEST (" + methodDescriptor.getFullMethodName() + "):", message);
        }
        super.sendMessage(message);
      }

      @Override
      public void start(Listener responseListener, Metadata headers) {
        ForwardingClientCallListener<Object> clientCallListener = new ForwardingClientCallListener<>() {

          @Override
          public void onMessage(Object message) {
            if (TRACE) {
              traceOut("RESPONSE (" + methodDescriptor.getFullMethodName() + "):", message);
            }
            super.onMessage(message);
          }

          @Override
          protected Listener<Object> delegate() {
            return responseListener;
          }
        };
        super.start(clientCallListener, headers);
      }
    };
  }
}

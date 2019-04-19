package com.avast.grpc.jwt.server;

import com.avast.grpc.jwt.Constants;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class JwtServerInterceptorTest {

  JwtTokenParser<String> jwtTokenParser = jwtToken -> jwtToken;
  JwtServerInterceptor<String> target = new JwtServerInterceptor<>(jwtTokenParser);
  ServerCall<Object, Object> serverCall = (ServerCall<Object, Object>) mock(ServerCall.class);
  ServerCallHandler<Object, Object> next =
      (ServerCallHandler<Object, Object>) mock(ServerCallHandler.class);

  @Test
  public void closesCallOnMisingHeader() {
    target.interceptCall(serverCall, new Metadata(), next);
    verify(serverCall).close(any(), any());
    verify(next, never()).startCall(any(), any());
  }

  @Test
  public void closesCallOnInvalidHeader() {
    Metadata metadata = new Metadata();
    metadata.put(Constants.AuthorizationMetadataKey, "Bbb");
    target.interceptCall(serverCall, metadata, next);
    verify(serverCall).close(any(), any());
    verify(next, never()).startCall(any(), any());
  }

  @Test
  public void callNextStageWithContextKeyOnValidHeader() {
    Metadata metadata = new Metadata();
    metadata.put(Constants.AuthorizationMetadataKey, "Bearer test token");
    final AtomicReference<String> actualToken = new AtomicReference<>("");
    when(next.startCall(any(), any()))
        .thenAnswer(
            i -> {
              actualToken.set(target.AccessTokenContextKey.get());
              return null;
            });
    target.interceptCall(serverCall, metadata, next);
    verify(serverCall, never()).close(any(), any());
    verify(next).startCall(any(), any());
    assertEquals("test token", actualToken.get());
  }
}

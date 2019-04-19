package com.avast.grpc.jwt.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.avast.grpc.jwt.Constants;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class JwtCallCredentialsTest {

  AtomicReference<Metadata> actualMetadata = new AtomicReference<>();
  CallCredentials.MetadataApplier applier =
      new CallCredentials.MetadataApplier() {
        @Override
        public void apply(Metadata headers) {
          actualMetadata.set(headers);
        }

        @Override
        public void fail(Status status) {}
      };
  ExecutorService executor = Executors.newSingleThreadExecutor();

  @Test
  public void synchronous() {
    JwtCallCredentials target = JwtCallCredentials.synchronous(() -> "test token");
    target.applyRequestMetadata(
        mock(CallCredentials.RequestInfo.class), Executors.newSingleThreadExecutor(), applier);
    assertEquals("Bearer test token", actualMetadata.get().get(Constants.AuthorizationMetadataKey));
  }

  @Test
  public void blocking() throws InterruptedException {
    JwtCallCredentials target = JwtCallCredentials.blocking(() -> "test token");
    target.applyRequestMetadata(mock(CallCredentials.RequestInfo.class), executor, applier);
    Thread.sleep(1000);
    assertEquals("Bearer test token", actualMetadata.get().get(Constants.AuthorizationMetadataKey));
  }

  @Test
  public void asynchronous() throws InterruptedException {
    JwtCallCredentials target =
        JwtCallCredentials.asynchronous(() -> CompletableFuture.completedFuture("test token"));
    target.applyRequestMetadata(mock(CallCredentials.RequestInfo.class), executor, applier);
    Thread.sleep(1000);
    assertEquals("Bearer test token", actualMetadata.get().get(Constants.AuthorizationMetadataKey));
  }
}

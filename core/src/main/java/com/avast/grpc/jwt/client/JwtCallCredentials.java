package com.avast.grpc.jwt.client;

import com.avast.grpc.jwt.Constants;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.concurrent.Executor;

public abstract class JwtCallCredentials extends CallCredentials {

  public static JwtCallCredentials synchronous(SynchronousJwtTokenProvider tokenProvider) {
    return new Synchronous(tokenProvider);
  }

  public static JwtCallCredentials blocking(BlockingJwtTokenProvider tokenProvider) {
    return new Blocking(tokenProvider);
  }

  public static JwtCallCredentials asynchronous(AsyncJwtTokenProvider tokenProvider) {
    return new Asynchronous(tokenProvider);
  }

  @Override
  public void thisUsesUnstableApi() {}

  protected void applyToken(MetadataApplier applier, String jwtToken) {
    Metadata metadata = new Metadata();
    metadata.put(Constants.AuthorizationMetadataKey, "Bearer " + jwtToken);
    applier.apply(metadata);
  }

  protected void applyFailure(MetadataApplier applier, Throwable e) {
    applier.fail(
        Status.UNAUTHENTICATED
            .withDescription("An exception when obtaining JWT token")
            .withCause(e));
  }

  public static class Synchronous extends JwtCallCredentials {

    private final SynchronousJwtTokenProvider jwtTokenProvider;

    public Synchronous(SynchronousJwtTokenProvider jwtTokenProvider) {
      this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void applyRequestMetadata(
        RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
      try {
        applyToken(applier, jwtTokenProvider.get());
      } catch (RuntimeException e) {
        applyFailure(applier, e);
      }
    }
  }

  public static class Blocking extends JwtCallCredentials {

    private final BlockingJwtTokenProvider jwtTokenProvider;

    public Blocking(BlockingJwtTokenProvider jwtTokenProvider) {
      this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void applyRequestMetadata(
        RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
      appExecutor.execute(
          () -> {
            try {
              applyToken(applier, jwtTokenProvider.get());
            } catch (RuntimeException e) {
              applyFailure(applier, e);
            }
          });
    }
  }

  public static class Asynchronous extends JwtCallCredentials {

    private final AsyncJwtTokenProvider jwtTokenProvider;

    public Asynchronous(AsyncJwtTokenProvider jwtTokenProvider) {
      this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void applyRequestMetadata(
        RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
      jwtTokenProvider
          .get()
          .whenComplete(
              (token, e) -> {
                if (token != null) applyToken(applier, token);
                else applyFailure(applier, e);
              });
    }
  }
}

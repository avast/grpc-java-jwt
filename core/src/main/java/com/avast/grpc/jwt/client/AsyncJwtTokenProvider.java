package com.avast.grpc.jwt.client;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AsyncJwtTokenProvider {
  /* Gets encoded JWT token. */
  CompletableFuture<String> get();
}

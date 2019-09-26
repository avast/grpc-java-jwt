package com.avast.grpc.jwt.server;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface JwtTokenParser<T> {

  /** Get valid JWT token, throws an exception otherwise. */
  CompletableFuture<T> parseToValid(String jwtToken);
}

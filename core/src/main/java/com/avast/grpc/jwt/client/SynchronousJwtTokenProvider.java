package com.avast.grpc.jwt.client;

@FunctionalInterface
public interface SynchronousJwtTokenProvider {
  /* Gets encoded JWT token without blocking. */
  String get();
}

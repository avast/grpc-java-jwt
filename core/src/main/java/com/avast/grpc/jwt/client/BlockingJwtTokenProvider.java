package com.avast.grpc.jwt.client;

@FunctionalInterface
public interface BlockingJwtTokenProvider {
  /* Gets encoded JWT token, can block (e.g. perform blocking I/O). */
  String get();
}

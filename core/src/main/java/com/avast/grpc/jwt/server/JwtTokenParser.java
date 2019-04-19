package com.avast.grpc.jwt.server;

@FunctionalInterface
public interface JwtTokenParser<T> {

  /** Get valid JWT token, throws an exception otherwise. */
  T parseToValid(String jwtToken) throws Exception;
}

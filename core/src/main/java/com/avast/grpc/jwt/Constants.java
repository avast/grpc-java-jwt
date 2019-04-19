package com.avast.grpc.jwt;

import io.grpc.Metadata;

public final class Constants {
  private Constants() {}

  public static io.grpc.Metadata.Key<String> AuthorizationMetadataKey =
      Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
}

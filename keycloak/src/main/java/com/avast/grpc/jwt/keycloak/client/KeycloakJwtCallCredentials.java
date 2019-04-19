package com.avast.grpc.jwt.keycloak.client;

import com.avast.grpc.jwt.client.JwtCallCredentials;
import com.avast.grpc.jwt.keycloak.KeycloakFactory;
import com.typesafe.config.Config;
import org.keycloak.admin.client.Keycloak;

public class KeycloakJwtCallCredentials extends JwtCallCredentials.Blocking
    implements AutoCloseable {
  private final Keycloak keycloak;

  public KeycloakJwtCallCredentials(Keycloak keycloak) {
    super(() -> keycloak.tokenManager().getAccessTokenString());
    this.keycloak = keycloak;
  }

  @Override
  public void close() throws Exception {
    keycloak.close();
  }

  public static KeycloakJwtCallCredentials fromConfig(Config config) {
    return new KeycloakJwtCallCredentials(KeycloakFactory.fromConfig(config));
  }
}

package com.avast.grpc.jwt.keycloak;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

public final class KeycloakFactory {
  public static Config DefaultConfig =
      ConfigFactory.defaultReference().getConfig("keycloakDefaults");

  public static Keycloak fromConfig(Config config) {
    Config fc = config.withFallback(DefaultConfig);
    return KeycloakBuilder.builder()
        .clientId(fc.getString("clientId"))
        .clientSecret(fc.getString("clientSecret"))
        .grantType(fc.getString("grantType"))
        .username(fc.getString("username"))
        .password(fc.getString("password"))
        .realm(fc.getString("realm"))
        .serverUrl(fc.getString("serverUrl"))
        .build();
  }

  private KeycloakFactory() {}
}

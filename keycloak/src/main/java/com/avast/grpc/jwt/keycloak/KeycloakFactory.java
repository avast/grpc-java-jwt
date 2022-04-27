package com.avast.grpc.jwt.keycloak;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

public final class KeycloakFactory {

  public static final String KEYCLOAK_DEFAULTS_CONFIG_NAME = "keycloakDefaults";

  public static Keycloak fromConfig(Config config) {
    return fromConfig(config, Thread.currentThread().getContextClassLoader());
  }

  public static Keycloak fromConfig(Config config, ClassLoader contextClassLoader) {
    Config fc = config.withFallback(getDefaultConfig(contextClassLoader));
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

  public static Config getDefaultConfig(final ClassLoader classLoader) {
    return ConfigFactory.defaultReference(classLoader).getConfig(KEYCLOAK_DEFAULTS_CONFIG_NAME);
  }

  private KeycloakFactory() {}
}

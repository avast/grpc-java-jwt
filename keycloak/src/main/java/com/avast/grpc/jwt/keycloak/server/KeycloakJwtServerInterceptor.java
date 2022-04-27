package com.avast.grpc.jwt.keycloak.server;

import static com.avast.grpc.jwt.keycloak.KeycloakFactory.getDefaultConfig;

import com.avast.grpc.jwt.server.JwtServerInterceptor;
import com.avast.grpc.jwt.server.JwtTokenParser;
import com.typesafe.config.Config;
import java.time.Clock;
import org.keycloak.representations.AccessToken;

public class KeycloakJwtServerInterceptor extends JwtServerInterceptor<AccessToken> {
  public KeycloakJwtServerInterceptor(JwtTokenParser<AccessToken> tokenParser) {
    super(tokenParser);
  }

  public static KeycloakJwtServerInterceptor fromConfig(Config config) {
    return fromConfig(config, Thread.currentThread().getContextClassLoader());
  }

  public static KeycloakJwtServerInterceptor fromConfig(
      Config config, ClassLoader contextClassLoader) {
    Config fc = config.withFallback(getDefaultConfig(contextClassLoader));
    KeycloakPublicKeyProvider publicKeyProvider =
        new DefaultKeycloakPublicKeyProvider(
            fc.getString("serverUrl"),
            fc.getString("realm"),
            fc.getDuration("minTimeBetweenJwksRequests"),
            fc.getDuration("publicKeyCacheTtl"),
            Clock.systemUTC());
    KeycloakJwtTokenParser tokenParser =
        new KeycloakJwtTokenParser(
            fc.getString("serverUrl"),
            fc.getString("realm"),
            fc.getStringList("allowedIssuers"),
            publicKeyProvider);
    tokenParser = tokenParser.withExpectedAudience(fc.getString("expectedAudience"));
    tokenParser = tokenParser.withExpectedIssuedFor(fc.getString("expectedIssuedFor"));
    return new KeycloakJwtServerInterceptor(tokenParser);
  }
}

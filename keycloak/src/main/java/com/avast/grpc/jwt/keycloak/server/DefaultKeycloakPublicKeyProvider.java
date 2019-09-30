package com.avast.grpc.jwt.keycloak.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.util.JWKSUtils;

public class DefaultKeycloakPublicKeyProvider implements KeycloakPublicKeyProvider {

  private final String serverUrl;
  private final String realm;
  private final Duration minTimeBetweenJwksRequests;
  private final Duration publicKeyCacheTtl;
  private final Clock clock;

  private Map<String, PublicKey> currentKeys = new ConcurrentHashMap<>();
  private volatile Instant lastRequestTime = Instant.MIN;

  public DefaultKeycloakPublicKeyProvider(
      String serverUrl,
      String realm,
      Duration minTimeBetweenJwksRequests,
      Duration publicKeyCacheTtl,
      Clock clock) {
    this.serverUrl = serverUrl;
    this.realm = realm;
    this.minTimeBetweenJwksRequests = minTimeBetweenJwksRequests;
    this.publicKeyCacheTtl = publicKeyCacheTtl;
    this.clock = clock;
  }

  @Override
  public PublicKey get(String keyId) {
    if (lastRequestTime.plus(publicKeyCacheTtl).isBefore(clock.instant())) {
      updateKeys();
    }
    PublicKey fromCache = currentKeys.get(keyId);
    if (fromCache != null) {
      return fromCache;
    }
    updateKeys();
    PublicKey res = currentKeys.get(keyId);
    if (res == null) {
      throw new RuntimeException("Key with following ID not found: " + keyId);
    }
    return res;
  }

  protected void updateKeys() {
    synchronized (this) {
      if (clock.instant().isAfter(lastRequestTime.plus(minTimeBetweenJwksRequests))) {
        Map<String, PublicKey> newKeys = fetchNewKeys();
        currentKeys.clear();
        currentKeys.putAll(newKeys);
        lastRequestTime = clock.instant();
      }
    }
  }

  protected Map<String, PublicKey> fetchNewKeys() {
    try {
      ObjectMapper om = new ObjectMapper();
      String jwksUrl = serverUrl + ServiceUrlConstants.JWKS_URL.replace("{realm-name}", realm);
      JSONWebKeySet jwks = om.readValue(new URL(jwksUrl).openStream(), JSONWebKeySet.class);
      return JWKSUtils.getKeysForUse(jwks, JWK.Use.SIG);
    } catch (IOException e) {
      throw new RuntimeException("Cannot fetch key from Keycloak server", e);
    }
  }
}

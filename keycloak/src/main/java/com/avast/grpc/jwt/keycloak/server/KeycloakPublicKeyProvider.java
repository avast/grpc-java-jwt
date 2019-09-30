package com.avast.grpc.jwt.keycloak.server;

import java.security.PublicKey;

public interface KeycloakPublicKeyProvider {
  PublicKey get(String keyId);
}

package com.avast.grpc.jwt.keycloak.server;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.JsonWebToken;

public class IssuersCheck implements TokenVerifier.Predicate<JsonWebToken> {
  private final String[] issuers;

  public IssuersCheck(String[] issuers) {
    this.issuers = issuers;
  }

  @Override
  public boolean test(JsonWebToken t) throws VerificationException {
    for (String i : issuers) {
      if (i.equals(t.getIssuer())) {
        return true;
      }
    }
    throw new VerificationException(
        "Invalid token issuer. Was '"
            + t.getIssuer()
            + "' but expected one of: "
            + String.join(" ", issuers));
  }
}
;

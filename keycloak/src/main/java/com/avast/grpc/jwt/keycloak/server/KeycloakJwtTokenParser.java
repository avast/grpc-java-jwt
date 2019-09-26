package com.avast.grpc.jwt.keycloak.server;

import com.avast.grpc.jwt.server.JwtTokenParser;
import com.google.common.base.Strings;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.TokenUtil;

public class KeycloakJwtTokenParser implements JwtTokenParser<AccessToken> {

  protected final KeycloakPublicKeyProvider publicKeyProvider;
  protected final TokenVerifier.Predicate<AccessToken>[] checks;
  protected String expectedAudience;
  protected String expectedIssuedFor;

  public KeycloakJwtTokenParser(
      String serverUrl, String realm, KeycloakPublicKeyProvider publicKeyProvider) {
    this.publicKeyProvider = publicKeyProvider;
    String realmUrl =
        serverUrl + ServiceUrlConstants.REALM_INFO_PATH.replace("{realm-name}", realm);
    this.checks =
        new TokenVerifier.Predicate[] {
          new TokenVerifier.RealmUrlCheck(realmUrl),
          TokenVerifier.SUBJECT_EXISTS_CHECK,
          new TokenVerifier.TokenTypeCheck(TokenUtil.TOKEN_TYPE_BEARER),
          TokenVerifier.IS_ACTIVE
        };
  }

  @Override
  public CompletableFuture<AccessToken> parseToValid(String jwtToken) {
    TokenVerifier<AccessToken> verifier;
    try {
      verifier = createTokenVerifier(jwtToken);
    } catch (VerificationException e) {
      CompletableFuture<AccessToken> r = new CompletableFuture<>();
      r.completeExceptionally(e);
      return r;
    }
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return verifier.verify().getToken();
          } catch (VerificationException e) {
            throw new CompletionException(e);
          }
        });
  }

  protected TokenVerifier<AccessToken> createTokenVerifier(String jwtToken)
      throws VerificationException {
    TokenVerifier<AccessToken> verifier =
        TokenVerifier.create(jwtToken, AccessToken.class).withChecks(checks);
    if (!Strings.isNullOrEmpty(expectedAudience)) {
      verifier = verifier.audience(expectedAudience);
    }
    if (!Strings.isNullOrEmpty(expectedIssuedFor)) {
      verifier = verifier.issuedFor(expectedIssuedFor);
    }
    verifier.publicKey(publicKeyProvider.get(verifier.getHeader().getKeyId()));
    return verifier;
  }

  public KeycloakJwtTokenParser withExpectedAudience(String expectedAudience) {
    this.expectedAudience = expectedAudience;
    return this;
  }

  public KeycloakJwtTokenParser withExpectedIssuedFor(String expectedIssuedFor) {
    this.expectedIssuedFor = expectedIssuedFor;
    return this;
  }
}

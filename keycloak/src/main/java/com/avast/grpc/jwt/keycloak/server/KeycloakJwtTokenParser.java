package com.avast.grpc.jwt.keycloak.server;

import com.avast.grpc.jwt.server.JwtTokenParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.security.PublicKey;

import com.google.common.base.Strings;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.TokenUtil;

public class KeycloakJwtTokenParser implements JwtTokenParser<AccessToken> {

  protected final PublicKey publicKey;
  protected final TokenVerifier.Predicate<AccessToken>[] checks;
  protected String expectedAudience;
  protected String expectedIssuedFor;

  protected KeycloakJwtTokenParser(String serverUrl, String realm, PublicKey publicKey) {
    this.publicKey = publicKey;
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
  public AccessToken parseToValid(String jwtToken) throws VerificationException {
    TokenVerifier<AccessToken> verifier = createTokenVerifier(jwtToken);
    return verifier.verify().getToken();
  }

  protected TokenVerifier<AccessToken> createTokenVerifier(String jwtToken) {
    TokenVerifier<AccessToken> verifier = TokenVerifier.create(jwtToken, AccessToken.class).withChecks(checks).publicKey(publicKey);
    if (!Strings.isNullOrEmpty(expectedAudience)) {
      verifier = verifier.audience(expectedAudience);
    }
    if (!Strings.isNullOrEmpty(expectedIssuedFor)) {
      verifier = verifier.issuedFor(expectedIssuedFor);
    }
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

  public static KeycloakJwtTokenParser create(String serverUrl, String realm) {
    try {
      ObjectMapper om = new ObjectMapper();
      String jwksUrl = serverUrl + ServiceUrlConstants.JWKS_URL.replace("{realm-name}", realm);
      JSONWebKeySet jwks = om.readValue(new URL(jwksUrl).openStream(), JSONWebKeySet.class);
      if (jwks.getKeys().length == 0) {
        throw new RuntimeException("No keys found");
      }
      JWK jwk = jwks.getKeys()[0];
      PublicKey publicKey = JWKParser.create(jwk).toPublicKey();
      return new KeycloakJwtTokenParser(serverUrl, realm, publicKey);
    } catch (Exception e) {
      throw new RuntimeException("Exception when obtaining public key from " + serverUrl, e);
    }
  }
}

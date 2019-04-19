# gRPC Java JWT support
[![Build Status](https://travis-ci.org/avast/grpc-java-jwt.svg?branch=master)](https://travis-ci.org/avast/grpc-java-jwt) [![Download](https://api.bintray.com/packages/avast/maven/grpc-java-jwt/images/download.svg) ](https://bintray.com/avast/maven/grpc-java-jwt/_latestVersion)

Library that helps with authenticated communication in gRPC-Java based applications. It uses [JSON Web Token](https://jwt.io/) transported in `Authorization` header (as `Bearer rawJWT`).

Implementation of standard `CallCredentials` ensures that the header is sent, and `ServerInterceptor` ensures that the incoming header is valid and makes the parsed JWT available for the underlying code.

```maven
<dependency>
  <groupId>com.avast.grpc</groupId>
  <artifactId>grpc-java-jwt</artifactId>
  <version>$latestVersion</version>
</dependency>
```
```gradle
compile "com.avast.grpc:grpc-java-jwt:$latestVersion"
````

Please note that all artifacts are published to [Avast Bintray respository](https://bintray.com/avast/maven/grpc-java-jwt).
 Mirroring to [JCenter](https://bintray.com/bintray/jcenter) is pending, so now you have to explicitly add this repository to your project.

## Keycloak support
There are implementations of the core interfaces for [Keycloak](https://www.keycloak.org/).

```maven
<dependency>
  <groupId>com.avast.grpc</groupId>
  <artifactId>grpc-java-jwt-keycloak</artifactId>
  <version>$latestVersion</version>
</dependency>
```
```gradle
compile "com.avast.grpc:grpc-java-jwt-keycloak:$latestVersion"
````

### Client usage
This ensures that each call contains `Authorization` header with `Bearer ` prefixed Keycloak access token (as JWT).
```java
import com.avast.grpc.jwt.keycloak.client.KeycloakJwtCallCredentials;

KeycloakJwtCallCredentials callCredentials = KeycloakJwtCallCredentials.fromConfig(yourConfig);
YourService.newStub(aChannel).withCallCredentials(callCredentials);
```

### Server usage
This ensures that only requests with valid `JWT` in `Authorization` header are processed.
 The `fromConfig` method automatically downloads the Keycloak public key before the instance is actually created.
```java
import io.grpc.ServerServiceDefinition;
import com.avast.grpc.jwt.keycloak.server.KeycloakJwtServerInterceptor;

KeycloakJwtServerInterceptor serverInterceptor = KeycloakJwtServerInterceptor.fromConfig(yourConfig);
ServerServiceDefinition interceptedService = ServerInterceptors.intercept(yourService, serverInterceptor);

// read token in a gRPC method implementation
import org.keycloak.representations.AccessToken;
AccessToken accessToken = serverInterceptor.AccessTokenContextKey.get();
```

There is also [this integration test](keycloak/src/test/java/com/avast/grpc/jwt/keycloak/KeycloakTest.java) that can serve as nice example.

## Implementation notes

On the client side, there is implementation of `CallCredentials` that ensures the JWT token is correctly stored to the headers. Just call a static method on [JwtCallCredentials](core/src/main/java/com/avast/grpc/jwt/client/JwtCallCredentials.java) - it will require an instance of a _JwtTokenProvider_ (an interface that returns encoded JWT).

On server side, there is `ServerInterceptor` implementation that parses the incoming JWT and verifies it. [JwtServerInterceptor](core/src/main/java/com/avast/grpc/jwt/server/JwtServerInterceptor.java) requires an instance of [JwtTokenParser](core/src/main/java/com/avast/grpc/jwt/server/JwtTokenParser.java) - it's an interface that parses and verifies the JWT.

## About gRPC internals
gRCP uses terms `Metadata` and `Context keys`. `Metadata` is set of key-value pairs that are transported between client and server, et vice versa. So it's like HTTP headers.

On other hand, `Context key` is set of values that are available during request processing.
 By default, a `Storage` implementation based on `ThreadLocal` is used.
 Thanks to this, you can just call `get()` method on a Context key and you immediately get the value because it read the value from `Context.current()`.
 
So when implementing interceptors, you must be sure that you read Context values from the right thread. It's actually no issue for us because:
1. The right thread is automatically handled by gRPC-core when using`CallCredentials`. So you can call `applier.apply()` method on any thread.
2. Our `ServerInterceptor` implementation is fully synchronous.

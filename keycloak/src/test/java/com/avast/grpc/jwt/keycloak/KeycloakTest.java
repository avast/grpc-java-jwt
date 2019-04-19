package com.avast.grpc.jwt.keycloak;

import com.avast.grpc.jwt.keycloak.client.KeycloakJwtCallCredentials;
import com.avast.grpc.jwt.keycloak.server.KeycloakJwtServerInterceptor;
import com.avast.grpc.jwt.test.TestServiceGrpc;
import com.avast.grpc.jwt.test.TestServices;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KeycloakTest {
  @Test
  public void endToEndTest() throws IOException {
    Config config = ConfigFactory.load().getConfig("testKeycloak");
    String channelName = InProcessServerBuilder.generateName();
    ManagedChannel clientChannel =
        InProcessChannelBuilder.forName(channelName).usePlaintext().build();

    TestServiceGrpc.TestServiceBlockingStub client =
        TestServiceGrpc.newBlockingStub(clientChannel)
            .withCallCredentials(KeycloakJwtCallCredentials.fromConfig(config));

    KeycloakJwtServerInterceptor serverInterceptor =
        KeycloakJwtServerInterceptor.fromConfig(config);
    TestServiceImpl service = new TestServiceImpl(serverInterceptor.AccessTokenContextKey);
    Server server =
        InProcessServerBuilder.forName(channelName)
            .addService(ServerInterceptors.intercept(service, serverInterceptor))
            .build()
            .start();

    TestServices.AddResponse sum =
        client.add(TestServices.AddParams.newBuilder().setA(1).setB(2).build());

    assertEquals(sum.getSum(), 3);
    assertEquals(service.lastAccessToken.getType(), "Bearer");
  }
}

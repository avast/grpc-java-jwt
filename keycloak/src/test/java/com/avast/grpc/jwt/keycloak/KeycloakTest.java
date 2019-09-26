package com.avast.grpc.jwt.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.avast.grpc.jwt.keycloak.client.KeycloakJwtCallCredentials;
import com.avast.grpc.jwt.keycloak.server.KeycloakJwtServerInterceptor;
import com.avast.grpc.jwt.test.TestServiceGrpc;
import com.avast.grpc.jwt.test.TestServices;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import org.junit.Test;

public class KeycloakTest {
  Config config = ConfigFactory.load().getConfig("testKeycloak");
  String channelName = InProcessServerBuilder.generateName();

  ManagedChannel clientChannel =
      InProcessChannelBuilder.forName(channelName).usePlaintext().build();

  KeycloakJwtServerInterceptor serverInterceptor = KeycloakJwtServerInterceptor.fromConfig(config);
  TestServiceImpl service = new TestServiceImpl(serverInterceptor.AccessTokenContextKey);

  @Test
  public void endToEndTest() throws IOException {
    TestServiceGrpc.TestServiceBlockingStub client =
        TestServiceGrpc.newBlockingStub(clientChannel)
            .withCallCredentials(KeycloakJwtCallCredentials.fromConfig(config));

    Server server =
        InProcessServerBuilder.forName(channelName)
            .addService(ServerInterceptors.intercept(service, serverInterceptor))
            .build()
            .start();
    try {
      TestServices.AddResponse sum =
          client.add(TestServices.AddParams.newBuilder().setA(1).setB(2).build());
      assertEquals(sum.getSum(), 3);
      assertEquals(service.lastAccessToken.getType(), "Bearer");
    } finally {
      server.shutdownNow();
    }
  }

  @Test
  public void rejectsRequestWithoutHeader() throws IOException {
    TestServiceGrpc.TestServiceBlockingStub client = TestServiceGrpc.newBlockingStub(clientChannel);
    Server server =
        InProcessServerBuilder.forName(channelName)
            .addService(ServerInterceptors.intercept(service, serverInterceptor))
            .build()
            .start();
    try {
      TestServices.AddResponse sum =
          client.add(TestServices.AddParams.newBuilder().setA(1).setB(2).build());
    } catch (StatusRuntimeException e) {
      assertFalse(e.getStatus().isOk());
      assertEquals(Status.UNAUTHENTICATED.getCode(), e.getStatus().getCode());
    } finally {
      server.shutdownNow();
    }
  }
}

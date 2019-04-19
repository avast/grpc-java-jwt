package com.avast.grpc.jwt.keycloak;

import com.avast.grpc.jwt.test.TestServiceGrpc;
import com.avast.grpc.jwt.test.TestServices;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.keycloak.representations.AccessToken;

public class TestServiceImpl extends TestServiceGrpc.TestServiceImplBase {

  private final Context.Key<AccessToken> accessTokenKey;
  AccessToken lastAccessToken;

  public TestServiceImpl(Context.Key<AccessToken> accessTokenKey) {
    this.accessTokenKey = accessTokenKey;
  }

  @Override
  public void add(
      TestServices.AddParams request, StreamObserver<TestServices.AddResponse> responseObserver) {
    lastAccessToken = accessTokenKey.get();
    responseObserver.onNext(
        TestServices.AddResponse.newBuilder().setSum(request.getA() + request.getB()).build());
    responseObserver.onCompleted();
  }
}

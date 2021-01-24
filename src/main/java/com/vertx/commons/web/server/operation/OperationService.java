package com.vertx.commons.web.server.operation;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;

public abstract class OperationService {

  protected Vertx vertx;
  protected ServiceDiscovery serviceDiscovery;

  public final Future<Void> initialize(Vertx vertx, ServiceDiscovery serviceDiscovery) {
    this.vertx = vertx;
    this.serviceDiscovery = serviceDiscovery;
    return this.start();
  }

  private final Future<Void> start() {
    Promise<Void> promise = Promise.promise();
    this.up(promise);
    this.up();
    return promise.future();
  }

  protected void up() {
    // Nothing to do
  }

  protected void up(Promise<Void> promise) {
    promise.complete();
  }
}

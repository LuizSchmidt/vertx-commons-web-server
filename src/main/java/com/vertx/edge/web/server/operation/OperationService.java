/*
 * Vert.x Edge, open source.
 * Copyright (C) 2020-2021 Vert.x Edge
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vertx.edge.web.server.operation;

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

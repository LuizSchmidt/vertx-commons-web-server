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

import java.util.Map;

import com.vertx.edge.utils.CompositeFutureBuilder;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceBinderOperation {

  private Vertx vertx;
  private ServiceDiscovery discovery;
  private ServiceBinder serviceBinder;

  public static ServiceBinderOperation create(Vertx vertx, ServiceDiscovery discovery) {
    return new ServiceBinderOperation(vertx, discovery, new ServiceBinder(vertx));
  }

  public Future<Void> bindAll(Map<String, Class<?>> addresses) {
    CompositeFutureBuilder composite = CompositeFutureBuilder.create();

    addresses.forEach((address, clazz) -> {
      OperationService operation = bind(address, clazz);
      composite.add(operation.initialize(vertx, discovery)
          .onSuccess(v -> log.info("Publishing '{}' for address: {}", clazz.getSimpleName(), address)));
    });

    Promise<Void> promise = Promise.promise();
    composite.all().onSuccess(v -> log.info("All Controllers are mapped.")).onComplete(promise);
    return promise.future();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private OperationService bind(String address, Class<?> clazz) {
    OperationService operation = newInstance(clazz);

    Class interfaceClazz = operation.getClass().getInterfaces()[0];
    serviceBinder.setAddress(address).register(interfaceClazz, operation);
    return operation;
  }

  private static OperationService newInstance(Class<?> clazz) {
    try {
      return (OperationService) clazz.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException | SecurityException e) {
      throw new IllegalArgumentException(e);
    } catch (IllegalArgumentException e) {
      throw e;
    }
  }
}

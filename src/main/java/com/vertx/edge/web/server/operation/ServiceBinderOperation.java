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
import com.vertx.edge.utils.VoidFuture;

import io.vertx.core.Future;
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

  public static ServiceBinderOperation create(Vertx vertx, ServiceDiscovery discovery, boolean debugInfo) {
    return new ServiceBinderOperation(vertx, discovery, new ServiceBinder(vertx).setIncludeDebugInfo(debugInfo));
  }

  public Future<Void> bindAll(Map<String, Class<?>> addresses) {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName("serviceBinder");
    CompositeFutureBuilder composite = CompositeFutureBuilder.create();

    addresses.forEach((address, clazz) -> {
      OperationService operation = bind(address, clazz);
      composite.add(operation.initialize(vertx, discovery)
          .onSuccess(v -> log.debug("Publishing '{}' for address: {}", clazz.getSimpleName(), address)));
    });

    return composite.all().onSuccess(v -> log.info("all Controllers are mapped.")).compose(VoidFuture.future())
        .onComplete(v -> Thread.currentThread().setName(threadName));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private OperationService bind(String address, Class<?> clazz) {
    OperationService operation = newInstance(clazz);

    Class interfaceClazz = operation.getClass().getInterfaces()[0];

    try {
      serviceBinder.setAddress(address).register(interfaceClazz, operation);
    } catch (IllegalStateException e) {
      log.error("\n**************************************************\n\n" + "Detail: " + e.getMessage()
          + "\nThis class are generated from build, rebuild your application. \nFor more information "
          + "visit https://vertx.io/docs/vertx-service-proxy/java/#_code_generation"
          + "\n\n**************************************************\n");
      throw e;
    }
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

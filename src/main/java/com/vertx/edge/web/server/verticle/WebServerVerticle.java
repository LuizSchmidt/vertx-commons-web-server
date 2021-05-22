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
package com.vertx.edge.web.server.verticle;

import java.util.Map;

import com.vertx.edge.deploy.DeployerVerticle;
import com.vertx.edge.verticle.BaseVerticle;
import com.vertx.edge.web.server.operation.ServiceBinderOperation;
import com.vertx.edge.web.server.operation.mapper.OperationMapper;
import com.vertx.edge.web.server.response.FailureResponse;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouterImpl;
import io.vertx.ext.web.openapi.RouterBuilder;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author Luiz Schmidt
 *
 */
@Log4j2
public class WebServerVerticle extends BaseVerticle {

  private static final int DEFAULT_HTTP_PORT = 443;
  private static final String DEFAULT_SPEC_PATH = "src/main/resources/spec.yaml";
  private Map<String, Class<?>> operations;

  @Override
  protected void up(Promise<Void> promise) {
    Integer port = this.config().getInteger("port", DEFAULT_HTTP_PORT);
    String specPath = this.config().getString("specPath", DEFAULT_SPEC_PATH);
    String basePackage = this.config().getString(DeployerVerticle.BASE_PACKAGE);
    boolean debug = this.config().getBoolean("debug", false);

    operations = OperationMapper.mapFromAnnotation(basePackage);

    ServiceBinderOperation.create(vertx, discovery, debug).bindAll(operations)
        .compose(v -> this.startWebServer(port, specPath)).onComplete(promise);
  }

  private Future<Void> startWebServer(Integer port, String specPath) {
    Promise<Void> promise = Promise.promise();
    RouterBuilder.create(vertx, specPath).compose(routerBuilder -> {
      Router router = buildRoutes(routerBuilder);

      HttpServerOptions httpOptions = new HttpServerOptions().setPort(port);
      return vertx.createHttpServer(httpOptions).requestHandler(router).listen();
    }).onSuccess(v -> {
      String name = Thread.currentThread().getName();
      Thread.currentThread().setName("web-server");
      log.info("Web Server opened at port {}", port);
      Thread.currentThread().setName(name);
      promise.complete();
    }).onFailure(promise::fail);
    return promise.future();
  }

  private Router buildRoutes(RouterBuilder routerBuilder) {
    operations.forEach((k, v) -> {
      log.info("Addresses: " + k);
      routerBuilder.mountServiceInterface(v, k);
    });
    
    RouterImpl router = (RouterImpl) routerBuilder.createRouter()
          .errorHandler(HttpResponseStatus.BAD_REQUEST.code(), 
              rc -> this.fail(HttpResponseStatus.BAD_REQUEST, rc))
          .errorHandler(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), 
              rc -> this.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR, rc))
          .errorHandler(HttpResponseStatus.NOT_FOUND.code(), 
              rc -> this.fail(HttpResponseStatus.NOT_FOUND, rc))
          .errorHandler(HttpResponseStatus.NOT_IMPLEMENTED.code(), 
              rc -> this.fail(HttpResponseStatus.NOT_IMPLEMENTED, rc));

    /**
     * TODO O ultimo handler setado pelo Operation, não faz o redirect para o next() com isso, não chega nesse ponto.
     * 
     * Verificar forma de capturar o rc.end() ou nao teremos log da saida.
     * 
     * router.route().last().handler(this::log);
     */
    return router;
  }

//  private void log(RoutingContext rc) {
//    if (rc.failure() == null && this.config().getBoolean("debug", false).booleanValue()) {
//      log.info("[{}] {} -> response {}", rc.request().method(), rc.normalizedPath(), rc.response().getStatusCode());
//    }
//    rc.next();
//  }
  
  public void fail(HttpResponseStatus httpStatusCode, RoutingContext rc) {
    HttpServerResponse response = rc.response()
        .putHeader("Content-Type", "application/json")
        .setStatusCode(httpStatusCode.code())
        .setStatusMessage(httpStatusCode.reasonPhrase());

    JsonObject body;
    String message = httpStatusCode.reasonPhrase();
    if (rc.failure() != null) {
      FailureResponse failureResponse = FailureResponse.create(rc.failure());
      if(failureResponse.getCode() == 0) {
        failureResponse.setCode(httpStatusCode.code());
      }
      message = failureResponse.getMessage();
      body = JsonObject.mapFrom(failureResponse);
      response.end(body.toBuffer());
    } else {
      response.end();
    }
    
    if (this.config().getBoolean("debug", false).booleanValue()) {
      log.info("[{}] {} -> {}:response {}", rc.request().method(), rc.normalizedPath(), rc.response().getStatusCode(),
          message);
    }
  }
}

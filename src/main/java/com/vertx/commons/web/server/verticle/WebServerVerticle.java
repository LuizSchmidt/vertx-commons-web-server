package com.vertx.commons.web.server.verticle;

import java.util.Map;

import com.vertx.commons.verticle.BaseVerticle;
import com.vertx.commons.web.server.operation.ServiceBinderOperation;
import com.vertx.commons.web.server.operation.mapper.OperationMapper;
import com.vertx.commons.web.server.response.ResponseBuilder;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
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
    String basePackage = this.config().getString("base-package");

    operations = OperationMapper.mapFromAnnotation(basePackage);

    ServiceBinderOperation.create(vertx, discovery).bindAll(operations)
        .compose(v -> this.startWebServer(port, specPath)).onComplete(promise).onFailure(promise::fail);
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

    return routerBuilder.createRouter()
          .errorHandler(HttpResponseStatus.BAD_REQUEST.code(), ResponseBuilder::failedResponse)
          .errorHandler(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ResponseBuilder::failedResponse)
          .errorHandler(HttpResponseStatus.NOT_FOUND.code(), ResponseBuilder::failedResponse);
  }
}

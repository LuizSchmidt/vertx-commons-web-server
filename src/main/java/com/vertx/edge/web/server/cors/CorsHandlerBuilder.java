package com.vertx.edge.web.server.cors;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Luiz Schmidt
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorsHandlerBuilder {

  /**
   * Constructor from json
   * ({@link https://fetch.spec.whatwg.org/#http-cors-protocol}
   * 
   * "cors" : {
   *    "origins" : ["*"],
   *    "allowedHeaders" : ["foo"],
   *    "allowedMethods" : ["GET"],  // see ({@link #HttpMethod}
   *    "allowCredentials" : true,
   *    "exposedHeader" : "bar",
   *    "maxAgeSeconds" : 60
   * }
   * 
   * @param config
   * @return
   */
  public static Handler<RoutingContext> fromJson(JsonObject config) {
    CorsHandler cors = CorsHandler.create();

    if (config == null || config.isEmpty()) {
      return cors;
    }

    config.getJsonArray("origins", new JsonArray()).stream().map(Object::toString).forEach(cors::addOrigin);
    config.getJsonArray("allowHeaders", new JsonArray()).stream().map(Object::toString).forEach(cors::allowedHeader);
    config.getJsonArray("allowedMethods", new JsonArray()).stream().map(Object::toString).map(HttpMethod::valueOf)
        .forEach(cors::allowedMethod);

    if (config.containsKey("allowCredentials")) {
      cors.allowCredentials(config.getBoolean("allowCredentials"));
    }
    if (config.containsKey("exposedHeader")) {
      cors.exposedHeader(config.getString("exposedHeader"));
    }
    if (config.containsKey("maxAgeSeconds")) {
      cors.maxAgeSeconds(config.getInteger("maxAgeSeconds"));
    }

    return cors;
  }

}

package com.vertx.commons.web.server.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseBuilder {

  public static final HttpResponseStatus HTTP_500 = HttpResponseStatus.INTERNAL_SERVER_ERROR;
  public static final String ERROR_DEFAULT_CONTENT_TYPE = "application/json";

  public static void failedResponse(RoutingContext rc) {
    if (rc.failure() != null) {
      rc.response().end(JsonObject.mapFrom(new FailureResponse().create(rc.failure())).toBuffer());
    } else {
      rc.response()
          .end(JsonObject.mapFrom(FailureResponse.builder().code(rc.response().getStatusCode()).build()).toBuffer());
    }
  }
}

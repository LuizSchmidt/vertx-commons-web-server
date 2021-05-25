package com.vertx.edge.web.server.response.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
public class HttpServiceException extends RuntimeException {

  private final transient HttpResponseStatus httpCode;
  private final transient JsonObject detail;

  public HttpServiceException(HttpResponseStatus httpCode, String message, JsonObject detail) {
    super(message);
    this.httpCode = httpCode;
    this.detail = detail;
  }
}

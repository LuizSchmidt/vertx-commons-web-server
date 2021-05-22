package com.vertx.edge.web.server.response.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
public class HttpServiceException extends RuntimeException {

  private static final long serialVersionUID = 8088095520054273553L;
  
  private HttpResponseStatus httpCode;
  private JsonObject detail;
  
  public HttpServiceException(HttpResponseStatus httpCode, String message, JsonObject detail) {
    super(message);
    this.httpCode = httpCode;
    this.detail = detail;
  }
}

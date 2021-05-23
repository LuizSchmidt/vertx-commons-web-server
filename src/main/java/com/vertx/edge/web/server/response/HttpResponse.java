package com.vertx.edge.web.server.response;

import com.vertx.edge.web.server.response.exception.HttpServiceException;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpResponse {

  public static HttpServiceException notFound() {
    return response(HttpResponseStatus.NOT_FOUND);
  }

  public static HttpServiceException notFound(String message) {
    return response(HttpResponseStatus.NOT_FOUND, message);
  }

  public static HttpServiceException conflict() {
    return response(HttpResponseStatus.CONFLICT);
  }

  public static HttpServiceException conflict(String message) {
    return response(HttpResponseStatus.CONFLICT, message);
  }
  
  public static HttpServiceException badRequest(String message, JsonObject detail) {
    return new HttpServiceException(HttpResponseStatus.BAD_REQUEST, message, detail);
  }

  private static HttpServiceException response(HttpResponseStatus httpStatus) {
    return response(httpStatus, httpStatus.reasonPhrase());
  }

  private static HttpServiceException response(HttpResponseStatus httpStatus, String message) {
    return response(httpStatus, message, null);
  }

  private static HttpServiceException response(HttpResponseStatus httpStatus, String message, JsonObject detail) {
    return new HttpServiceException(httpStatus, message, detail);
  }
}

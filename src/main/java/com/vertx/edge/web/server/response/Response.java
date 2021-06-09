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
package com.vertx.edge.web.server.response;

import com.vertx.edge.web.server.response.exception.HttpServiceException;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Response {

  /**
   * Create a 201 Created HTTP response with a header Location
   * 
   * @param location
   * @return a http {@link ServiceResponse}
   */
  public static Future<ServiceResponse> created(String location) {
    ServiceResponse serviceResponse = new ServiceResponse().setStatusCode(HttpResponseStatus.CREATED.code())
        .setStatusMessage(HttpResponseStatus.CREATED.reasonPhrase())
        .putHeader(HttpHeaderNames.LOCATION.toString(), location);
    return Future.succeededFuture(serviceResponse);
  }

  /**
   * Create a 200 Created HTTP response with a body JsonObject
   * 
   * @param location
   * @return a http {@link ServiceResponse}
   */
  public static Future<ServiceResponse> ok(JsonObject json) {
    return Future.succeededFuture(ServiceResponse.completedWithJson(json));
  }

  /**
   * Create a 200 Created HTTP response with a body JsonArray
   * 
   * @param location
   * @return a http {@link ServiceResponse}
   */
  public static Future<ServiceResponse> ok(JsonArray list) {
    return Future.succeededFuture(ServiceResponse.completedWithJson(list));
  }

  /**
   * Create a 204 No-Content HTTP response
   * 
   * @param location
   * @return a http {@link ServiceResponse}
   */
  public static Future<ServiceResponse> ok() {
    ServiceResponse serviceResponse = new ServiceResponse().setStatusCode(HttpResponseStatus.NO_CONTENT.code())
        .setStatusMessage(HttpResponseStatus.NO_CONTENT.reasonPhrase());
    return Future.succeededFuture(serviceResponse);
  }

  public static Future<ServiceResponse> fail(HttpResponseStatus httpCode, String message, JsonObject details) {
    return fail(httpCode, FailureResponse.create(httpCode.code(), message, details));
  }

  public static Future<ServiceResponse> fail(Throwable cause) {
    return fail(cause, FailureResponse.create(cause));
  }

  private static Future<ServiceResponse> fail(Throwable cause, FailureResponse failureResponse) {
    if (cause instanceof HttpServiceException) {
      HttpServiceException httpException = (HttpServiceException) cause;
      return fail(httpException.getHttpCode(), failureResponse);
    } else {
      return fail(HttpResponseStatus.INTERNAL_SERVER_ERROR, failureResponse);
    }
  }

  private static Future<ServiceResponse> fail(HttpResponseStatus httpStatus, FailureResponse failureResponse) {
    ServiceResponse response = ServiceResponse.completedWithJson(JsonObject.mapFrom(failureResponse));
    response.setStatusCode(httpStatus.code());
    response.setStatusMessage(httpStatus.reasonPhrase());
    return Future.succeededFuture(response);
  }
}

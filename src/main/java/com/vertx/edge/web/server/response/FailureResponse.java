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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.vertx.edge.web.server.response.exception.HttpServiceException;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.ValidationException;
import io.vertx.serviceproxy.ServiceException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FailureResponse {

  private int code;
  private String message;
  private JsonObject details;

  private FailureResponse(ServiceException cause) {
    this.code = cause.failureCode();
    this.details = cause.getDebugInfo();

    if (cause.failureType() == ReplyFailure.RECIPIENT_FAILURE && cause.getMessage() == null) {
      this.message = "Error during an unknown operation. This usually happens when exceptions are not handled.";
    } else {
      this.message = cause.getMessage();
    }
  }

  private FailureResponse(ValidationException cause) {
    this.code = HttpResponseStatus.BAD_REQUEST.code();
    this.message = cause.keyword() + " - " + cause.getMessage();
  }

  private FailureResponse(HttpServiceException cause) {
    this.message = cause.getMessage();
    this.details = cause.getDetail();
    this.code = cause.getHttpCode().code();
  }

  private FailureResponse(Throwable cause) {
    this.message = cause.getMessage();
  }

  public static FailureResponse create(Throwable cause) {
    if (cause instanceof HttpServiceException) {
      return new FailureResponse((HttpServiceException) cause);
    } else if (cause instanceof ValidationException) {
      return new FailureResponse((ValidationException) cause);
    } else if (cause instanceof ServiceException) {
      return new FailureResponse((ServiceException) cause);
    } else {
      return new FailureResponse(cause);
    }
  }

  public static FailureResponse create(int code, String message, JsonObject details) {
    return new FailureResponse(code, message, details);
  }
}

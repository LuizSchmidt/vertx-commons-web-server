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

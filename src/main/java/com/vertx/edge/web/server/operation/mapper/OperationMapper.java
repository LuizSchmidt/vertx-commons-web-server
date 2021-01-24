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
package com.vertx.edge.web.server.operation.mapper;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.vertx.edge.web.server.operation.Operation;

public final class OperationMapper {

  public static Map<String, Class<?>> mapFromAnnotation(String basePackage) {
    Reflections reflections = new Reflections(basePackage);

    return reflections.getTypesAnnotatedWith(Operation.class).stream()
        .collect(Collectors.toMap(clazz -> {
      return clazz.getAnnotation(Operation.class).value();
    }, Function.identity()));
  }
}

package com.vertx.commons.web.server.operation.mapper;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.vertx.commons.web.server.operation.Operation;

public final class OperationMapper {

  public static Map<String, Class<?>> mapFromAnnotation(String basePackage) {
    Reflections reflections = new Reflections(basePackage);

    return reflections.getTypesAnnotatedWith(Operation.class).stream()
        .collect(Collectors.toMap(clazz -> {
      return clazz.getAnnotation(Operation.class).value();
    }, Function.identity()));
  }
}

package com.hjzgg.apigateway.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {

  /**
   * 如果有值，表示用户对象的某个Bean Property
   */
  String value() default "";
}

package com.hjzgg.apigateway.swagger.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SwaggerRelatedClasses {
    Class<?>[] value() default {};
}

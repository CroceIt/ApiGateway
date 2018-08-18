package com.hjzgg.apigateway.api.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 禁止 springmvc
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ApigatewayNotMVCApplicationCondition.class)
public @interface ConditionalOnNotMVCApplication {
}
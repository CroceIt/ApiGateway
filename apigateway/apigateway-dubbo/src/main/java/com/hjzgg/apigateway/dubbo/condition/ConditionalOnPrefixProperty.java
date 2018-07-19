package com.hjzgg.apigateway.dubbo.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPrefixPropertyCondition.class)
public @interface ConditionalOnPrefixProperty {
    /**
     * A prefix that should be applied to each property. The prefix automatically ends
     * with a dot if not specified.
     * @return the prefix
     */
    String prefix() default "";
}
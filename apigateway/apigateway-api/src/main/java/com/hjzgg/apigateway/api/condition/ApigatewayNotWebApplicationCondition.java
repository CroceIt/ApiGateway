package com.hjzgg.apigateway.api.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * @author hujunzheng
 * @create 2018-07-25 18:25
 **/
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApigatewayNotWebApplicationCondition extends SpringBootCondition {
    private static final Logger logger = LoggerFactory.getLogger(ApigatewayNotWebApplicationCondition.class);

    private static final String WEB_CONTEXT_CLASS = "org.springframework.web.context."
            + "support.GenericWebApplicationContext";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        boolean required = metadata
                .isAnnotated(ConditionalOnNotWebApplication.class.getName());
        if (!required) {
            String noMatchMsg = String.format("metadata is not annotated by %s", ClassUtils.getQualifiedName(ConditionalOnNotWebApplication.class));
            logger.info(noMatchMsg);
            return ConditionOutcome.noMatch(noMatchMsg);
        }
        ConditionOutcome outcome = isWebApplication(context, metadata);
        if (outcome.isMatch()) {
            logger.warn("检测到项目为web application，引入apigateway dubbo配置失败！！！");
        }
        return ConditionOutcome.inverse(outcome);
    }

    private ConditionOutcome isWebApplication(ConditionContext context,
                                              AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnNotWebApplication.class);
        if (!ClassUtils.isPresent(WEB_CONTEXT_CLASS, context.getClassLoader())) {
            return ConditionOutcome
                    .noMatch(message.didNotFind("web application classes").atAll());
        }
        if (context.getBeanFactory() != null) {
            String[] scopes = context.getBeanFactory().getRegisteredScopeNames();
            if (ObjectUtils.containsElement(scopes, "session")) {
                return ConditionOutcome.match(message.foundExactly("'session' scope"));
            }
        }
        if (context.getEnvironment() instanceof StandardServletEnvironment) {
            return ConditionOutcome
                    .match(message.foundExactly("StandardServletEnvironment"));
        }
        if (context.getResourceLoader() instanceof WebApplicationContext) {
            return ConditionOutcome.match(message.foundExactly("WebApplicationContext"));
        }
        return ConditionOutcome.noMatch(message.because("not a web application"));
    }
}
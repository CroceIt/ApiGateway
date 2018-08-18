package com.hjzgg.apigateway.api.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author hujunzheng
 * @create 2018-07-25 18:25
 **/

@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApigatewayNotMVCApplicationCondition extends SpringBootCondition {
    private static final Logger logger = LoggerFactory.getLogger(ApigatewayNotMVCApplicationCondition.class);

    /*
     * The bean name for a DispatcherServlet that will be mapped to the root URL "/"
     */
    public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

    public static final String DEFAULT_DISPATCHER_SERVLET_CLASS_NAME = "org.springframework.web.servlet.DispatcherServlet";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        boolean required = metadata
                .isAnnotated(ConditionalOnNotMVCApplication.class.getName());
        if (!required) {
            String noMatchMsg = String.format("metadata is not annotated by %s", ClassUtils.getQualifiedName(ConditionalOnNotMVCApplication.class));
            logger.info(noMatchMsg);
            return ConditionOutcome.noMatch(noMatchMsg);
        }
        ConditionOutcome outcome = isMVCApplication(context, metadata);
        if (outcome.isMatch()) {
            logger.warn("检测到项目包含SpringMVC，引入apigateway dubbo配置失败！！！" + outcome.getMessage());
        }
        return ConditionOutcome.inverse(outcome);
    }

    /**
     * @see org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration.DefaultDispatcherServletCondition
     * */
    private ConditionOutcome isMVCApplication(ConditionContext context,
                                              AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnNotMVCApplication.class, "Default DispatcherServlet");
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        if (ClassUtils.isPresent(DEFAULT_DISPATCHER_SERVLET_CLASS_NAME, ClassUtils.getDefaultClassLoader())) {
            Class<?> dispatcherServletClass = ClassUtils.resolveClassName(DEFAULT_DISPATCHER_SERVLET_CLASS_NAME, ClassUtils.getDefaultClassLoader());
            List<String> dispatchServletBeans = Arrays.asList(beanFactory
                    .getBeanNamesForType(dispatcherServletClass, false, false));
            if (dispatchServletBeans.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome.noMatch(message.found("springmvc dispatcher servlet bean")
                        .items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            //beanName == DEFAULT_DISPATCHER_SERVLET_BEAN_NAME， 但不是springmvc 的DispatcherServlet
            if (beanFactory.containsBean(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome
                        .match(message.found("non springmvc dispatcher servlet bean")
                                .items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            if (dispatchServletBeans.isEmpty()) {
                return ConditionOutcome
                        .match(message.didNotFind("springmvc dispatcher servlet beans").atAll());
            } else {
                return ConditionOutcome.match(message
                        .found("springmvc dispatcher servlet beans")
                        .items(ConditionMessage.Style.QUOTE, dispatchServletBeans));
            }
        }

        return ConditionOutcome.match(message.didNotFind("springmvc dispatcher servlet bean").atAll());
    }
}
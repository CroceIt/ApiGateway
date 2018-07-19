package com.hjzgg.apigateway.commons.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * spring应用上下文工具类
 */
@Component
public class ContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext; //应用上下文对象

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ContextUtils.applicationContext = applicationContext;
    }

    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(String beanName, Class<T> cls) {
        return applicationContext.getBean(beanName, cls);
    }

    public static <T> T getBean(Class<T> cls) {
        return applicationContext.getBean(cls);
    }

    public static List<?> getBeans(Class<? extends Annotation> annotationType) {
        return applicationContext.getBeansWithAnnotation(annotationType).values().stream().collect(Collectors.toList());
    }

    public static <T> List<T> getBeansOfType(Class<T> type) {
        return applicationContext.getBeansOfType(type).values().stream().collect(Collectors.toList());
    }

    public static <T> List<T> getBeansOfType(ApplicationContext context, Class<T> type) {
        return context.getBeansOfType(type).values().stream().collect(Collectors.toList());
    }
}
package com.hjzgg.apigateway.api.rpc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hujunzheng
 * @create 2018-08-19 12:17
 *
 **/
@Order
public class JsonRpcResolver implements BeanPostProcessor {

    private Set<Class<?>> interfaces = new HashSet<>();

    private Set<Object> objects = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotatedElementUtils.hasAnnotation(bean.getClass(), com.alibaba.dubbo.config.annotation.Service.class)
                || AnnotatedElementUtils.hasAnnotation(bean.getClass(), org.springframework.stereotype.Service.class)) {
            this.interfaces.addAll(ClassUtils.getAllInterfacesAsSet(bean));
            objects.add(bean);
        }
        return bean;
    }

    public Set<Class<?>> getInterfaces() {
        return interfaces;
    }

    public Set<Object> getObjects() {
        return objects;
    }
}
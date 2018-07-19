package com.hjzgg.apigateway.soa.proxy.provider;

import com.hjzgg.apigateway.commons.utils.ContextUtils;
import com.hjzgg.apigateway.soa.annotation.SOAImplements;
import com.hjzgg.apigateway.soa.exceptions.SoaException;
import com.hjzgg.apigateway.soa.proxy.DynamicProxyAdapter;
import org.springframework.aop.TargetClassAware;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ProviderDynamicProxy extends DynamicProxyAdapter {

    private static final ConcurrentHashMap<Class<?>, Object> map = new ConcurrentHashMap<>();

    public ProviderDynamicProxy(ProviderProxyFactoryBean factoryBean) {
        super(factoryBean);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (method.equals(TargetClassAware.class.getMethod("getTargetClass", new Class[]{}))) {
                return bean.getResourceClass();
            }
            return method.invoke(this.findRelevantServiceProvider(), args);
        } catch (Exception e) {
            throw new SoaException("500", String.format("%s %s %s"
                    , "invoke service proxy object error!"
                    , ClassUtils.getQualifiedName(this.bean.getResourceClass())
                    , method.getName()
            ), e);
        }
    }

    private Object findRelevantServiceProvider() throws SoaException {
        Class<?> resourceClass = super.bean.getResourceClass();

        if (!map.contains(resourceClass)) {
            List<?> objects = ContextUtils.getBeans(SOAImplements.class);
            Stream<?> stream = objects.stream()
                    .filter(serviceProvider -> resourceClass.isAssignableFrom(serviceProvider.getClass()));
            if (stream.count() > 1) {
                throw new SoaException(String.format(
                        "multiple relevant service provider found with annotation %s and interface is %s"
                        , ClassUtils.getQualifiedName(SOAImplements.class)
                        , ClassUtils.getQualifiedName(resourceClass))
                );
            }

            if (stream.count() == 1) {
                map.put(resourceClass, stream.findFirst().get());
            } else {
                if (objects.size() > 1) {
                    throw new SoaException(String.format(
                            "multiple relevant service provider found with annotation %s"
                            , ClassUtils.getQualifiedName(SOAImplements.class))
                    );
                }

                if (objects.size() == 1) {
                    map.put(resourceClass, objects.get(0));
                } else {
                    try {
                        Object object = ContextUtils.getBean(resourceClass);
                        map.put(resourceClass, object);
                    } catch (Exception e) {
                        throw new SoaException("500", String.format(
                                "find relevant service provider with interface %s error"
                                , ClassUtils.getQualifiedName(resourceClass)), e
                        );
                    }
                }
            }
        }

        return map.get(resourceClass);
    }
}

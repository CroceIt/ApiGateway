package com.hjzgg.apigateway.soa.proxy.provider;

import com.hjzgg.apigateway.soa.exceptions.SoaException;
import com.hjzgg.apigateway.soa.proxy.ProxyFactoryBean;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author hujunzheng
 * @create 2018-02-18 下午3:01
 **/
public class ProviderProxyFactoryBean extends ProxyFactoryBean {
    public ProviderProxyFactoryBean(Class<?> selfDynamicProxyClass, Class<?> resourceClass, String version, String group) throws SoaException {
        super(selfDynamicProxyClass, resourceClass, version, group);
    }

    @Override
    public Object getObject() throws Exception {
        return this.newInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return resourceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public static Class<?> getProxyClass(Class<?> resourceClass) {
        /**
         * @see com.hjzgg.apigateway.dubbo.configure.SelfDubboAnnotationBean#postProcessAfterInitialization(Object, String)
         * @see org.springframework.aop.support.AopUtils#isAopProxy(Object)
         * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)
         * @see com.hjzgg.apigateway.soa.proxy.provider.ProviderDynamicProxy#invoke(Object, Method, Object[])
         * */
        return Proxy.getProxyClass(ProviderProxyFactoryBean.class.getClassLoader(), new Class[]{resourceClass, SpringProxy.class, TargetClassAware.class});
    }
}

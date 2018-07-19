package com.hjzgg.apigateway.soa.proxy;

import com.hjzgg.apigateway.soa.exceptions.SoaException;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author hujunzheng
 * @create 2018-02-04 上午12:00
 **/
public class DynamicProxyAdapter implements InvocationHandler {
    public DynamicProxyAdapter(ProxyFactoryBean factoryBean) {
        this.bean = factoryBean;
    }

    protected ProxyFactoryBean  bean;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        throw new SoaException(String.format("%s %s %s", "你应当重写", ClassUtils.getQualifiedName(DynamicProxyAdapter.class), "invoke方法"));
    }

    public static Object createJDKProxy(ProxyFactoryBean factoryBean, Class<?> selfDynamicProxyClass) throws SoaException {
        if (!DynamicProxyAdapter.class.equals(selfDynamicProxyClass.getSuperclass())) {
            throw new SoaException(String.format("%s %s %s"
                    , ClassUtils.getQualifiedName(selfDynamicProxyClass)
                    , "需要继承"
                    , ClassUtils.getQualifiedName(DynamicProxyAdapter.class)
            ));
        }
        Object selfDynamicProxyInstance;
        try {
            selfDynamicProxyInstance = selfDynamicProxyClass.getConstructor(factoryBean.getClass()).newInstance(factoryBean);
        } catch (Exception e) {
            throw new SoaException("500", "动态代理类创建失败", e);
        }
        Object proxy = Proxy.newProxyInstance(selfDynamicProxyClass.getClassLoader(),
                new Class[]{factoryBean.getResourceClass()}, (InvocationHandler) selfDynamicProxyInstance);
        return proxy;
    }
}

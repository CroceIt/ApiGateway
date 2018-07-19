package com.hjzgg.apigateway.soa.proxy;

import com.hjzgg.apigateway.soa.exceptions.SoaException;
import com.hjzgg.apigateway.soa.proxy.consumer.ConsumerProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hujunzheng on 2017/7/7.
 */
public abstract class ProxyFactoryBean implements FactoryBean {

    protected Class<?> selfDynamicProxyClass;//代理类
    protected Class<?> resourceClass;//接口类
    protected String version;//dubbo 版本
    protected String group;//dubbo 分组
    protected Object proxy;//代理对象
    protected Method createProxyMethod;

    public ProxyFactoryBean(Class<?> selfDynamicProxyClass, Class<?> resourceClass, String version, String group) throws SoaException {
        if (Objects.isNull(selfDynamicProxyClass)) {
            throw new SoaException("selfDynamicProxyClass 动态代理类不能为null");
        }
        try {
            this.createProxyMethod = Stream.of(selfDynamicProxyClass.getMethods())
                    .filter(method -> Modifier.isStatic(method.getModifiers())
                            && Modifier.isPublic(method.getModifiers())
                            && !Modifier.isAbstract(method.getModifiers())
                            && method.getParameters().length == 2
                            && method.getParameters()[0].getType().equals(ProxyFactoryBean.class)
                            && method.getParameters()[1].getType().equals(Class.class)
                            && !method.getReturnType().equals(void.class))
                    .collect(Collectors.toList())
                    .get(0);
        } catch (Exception e) {
            throw new SoaException("500", String.format("%s %s %s和%s %s, %s %s"
                    , ClassUtils.getQualifiedName(selfDynamicProxyClass)
                    , " 没有参数类型是 "
                    , ClassUtils.getQualifiedName(ConsumerProxyFactoryBean.class)
                    , ClassUtils.getQualifiedName(Class.class)
                    , " 的、公共的、非抽象的、返回值非void的方法"
                    , "请将你的动态代理类继承"
                    , ClassUtils.getQualifiedName(DynamicProxyAdapter.class)
            ), e);
        }

        this.selfDynamicProxyClass = selfDynamicProxyClass;
        this.resourceClass = resourceClass;
        this.version = version;
        this.group = group;
    }

    protected Object newInstance() throws SoaException {
        if(this.isSingleton() && this.proxy != null) {
            return proxy;
        }
        synchronized (this) {
            Object target;
            try {
                target = this.createProxyMethod.invoke(null,this, selfDynamicProxyClass);
            } catch (Exception e) {
                throw new SoaException("500", String.format("%s %s %s"
                        , ClassUtils.getQualifiedName(selfDynamicProxyClass)
                        , createProxyMethod.getName()
                        , "创建代理类异常")
                        , e);
            }
            if(proxy == null) {
                proxy = target;
            }
            return target;
        }
    }

    public Class<?> getSelfDynamicProxyClass() {
        return selfDynamicProxyClass;
    }

    public void setSelfDynamicProxyClass(Class<?> selfDynamicProxyClass) {
        this.selfDynamicProxyClass = selfDynamicProxyClass;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
package com.hjzgg.apigateway.soa.proxy.consumer;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.hjzgg.apigateway.soa.exceptions.SoaException;
import com.hjzgg.apigateway.soa.proxy.DynamicProxyAdapter;
import com.hjzgg.apigateway.soa.proxy.ProxyFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hujunzheng on 2017/7/7.
 */
public class ConsumerProxyFactoryBean extends ProxyFactoryBean implements EnvironmentAware {

    ApplicationConfig requestApplicationConfig;
    MonitorConfig dubboMonitorConfig;
    ConsumerConfig dubboConsumerConfig;

    private String refUrl;//服务提供者地址

    public ConsumerProxyFactoryBean(Class<?> selfDynamicProxyClass, Class<?> resourceClass, String version, String group) throws SoaException {
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

    public String getRefUrl() {
        return refUrl;
    }

    public ApplicationConfig getRequestApplicationConfig() {
        return requestApplicationConfig;
    }

    public void setRequestApplicationConfig(ApplicationConfig requestApplicationConfig) {
        this.requestApplicationConfig = requestApplicationConfig;
    }

    public MonitorConfig getDubboMonitorConfig() {
        return dubboMonitorConfig;
    }

    public void setDubboMonitorConfig(MonitorConfig dubboMonitorConfig) {
        this.dubboMonitorConfig = dubboMonitorConfig;
    }

    public ConsumerConfig getDubboConsumerConfig() {
        return dubboConsumerConfig;
    }

    public void setDubboConsumerConfig(ConsumerConfig dubboConsumerConfig) {
        this.dubboConsumerConfig = dubboConsumerConfig;
    }

    @Override
    public void setEnvironment(Environment environment) {
        refUrl = environment.getProperty("spring.dubbo.providerRefUrl");
    }
}
package com.hjzgg.apigateway.soa.proxy.consumer;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.hjzgg.apigateway.commons.utils.ContextUtils;
import com.hjzgg.apigateway.soa.exceptions.SoaException;
import com.hjzgg.apigateway.soa.proxy.DynamicProxyAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class ConsumerDynamicProxy extends DynamicProxyAdapter {

    public ConsumerDynamicProxy(ConsumerProxyFactoryBean factoryBean) {
        super(factoryBean);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(this.fetchServiceProxyObject(), args);
        } catch (Exception e) {
            throw new SoaException("500", String.format("%s %s %s"
                    , "invoke service proxy object error!"
                    , ClassUtils.getQualifiedName(this.bean.getResourceClass())
                    , method.getName()
                    ), e);
        }
    }

    private Object fetchServiceProxyObject() {
        ConsumerProxyFactoryBean proxyFactoryBean = (ConsumerProxyFactoryBean) super.bean;
        ReferenceConfig rc = new ReferenceConfig();
        rc.setApplication(proxyFactoryBean.getRequestApplicationConfig());
        rc.setMonitor(proxyFactoryBean.getDubboMonitorConfig());
        /**
         * @see ReferenceConfig#checkDefault()
         * 自定义客户端连接配置
         * */
        rc.setConsumer(proxyFactoryBean.getDubboConsumerConfig());
        rc.setVersion(proxyFactoryBean.getVersion());
        if (StringUtils.isNotBlank(proxyFactoryBean.getGroup())) {
            rc.setGroup(proxyFactoryBean.getGroup());
        }
        rc.setInterface(proxyFactoryBean.getResourceClass());
        rc.setUrl(proxyFactoryBean.getRefUrl());

        return ContextUtils.getBean(ReferenceConfigCache.class).get(rc);
    }
}

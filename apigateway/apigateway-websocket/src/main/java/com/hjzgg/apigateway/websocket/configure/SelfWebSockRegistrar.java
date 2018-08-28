package com.hjzgg.apigateway.websocket.configure;

import com.hjzgg.apigateway.websocket.ifaces.IWebSocketProvide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author hujunzheng
 * @create 2018-08-26 15:07
 **/
public class SelfWebSockRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private static final Logger log = LoggerFactory.getLogger(SelfWebSockRegistrar.class);

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (Objects.isNull(beanFactory)) {
            log.info("不能启动 WebSockRegistrar， beanFactory is null");
            return;
        }
        Map<String, IWebSocketProvide> webSocketProvideMap = beanFactory.getBeansOfType(IWebSocketProvide.class);
        if (CollectionUtils.isEmpty(webSocketProvideMap)) {
            log.info("没有发现 websocket provider 配置");
            return;
        }

        webSocketProvideMap.values()
                .stream()
                .forEach(webSocketProvide -> {
                    log.info("发现websocket provider " + ClassUtils.getQualifiedName(webSocketProvide.getClass()));
                    webSocketProvide.registerWebSockets(registry);
                });
    }
}
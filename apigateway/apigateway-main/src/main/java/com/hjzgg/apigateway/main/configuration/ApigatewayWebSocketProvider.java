package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.soa.proxy.consumer.ConsumerProxyFactoryBean;
import com.hjzgg.apigateway.websocket.ifaces.IWebSocketProvide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.websocket.server.ServerEndpoint;
import java.util.stream.Collectors;

/**
 * @author hujunzheng
 * @create 2018-08-28 13:00
 **/
@Component
public class ApigatewayWebSocketProvider implements IWebSocketProvide, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApigatewayWebSocketProvider.class);

    private BeanDefinitionRegistry registry;

    @Override
    public void registerWebSockets(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() instanceof ApigatewayApplicationContext) {
            ApigatewayApplicationContext context = (ApigatewayApplicationContext) event.getApplicationContext();
            BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            context.getBeansOfType(ConsumerProxyFactoryBean.class)
                    .entrySet()
                    .stream()
                    .filter(entry -> {
                        Class<?> clazz = entry.getValue().getObjectType();//获取接口层信息
                        if (AnnotatedElementUtils.isAnnotated(clazz, ServerEndpoint.class)) {
                            return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toMap(entry -> entry.getValue().getObjectType(), entry -> {
                        //entry.getKey()是对应工厂beanName，前面多加了一个&符号。去掉了&才是工厂产生的代理bean的beanName
                        String realBeanName = entry.getKey().substring(1);
                        Object realBean = context.getBean(realBeanName);
                        if (context.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry) {
                            ((BeanDefinitionRegistry) context.getAutowireCapableBeanFactory()).removeBeanDefinition(realBeanName);
                        }
                        return realBean;
                    }))
                    .forEach((clazz, instance) -> {//clazz是带有ServerEndpoint的接口类型，instance是该接口对应的代理类实例
                        Class<?> wsClazz = generateWsClass(clazz, instance);
                        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(wsClazz).getBeanDefinition();
                        String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
                        if (registry.containsBeanDefinition(beanName)) {
                            log.debug(beanName + " already exists! Class is " + ClassUtils.getQualifiedName(wsClazz) + " .");
                            return;
                        }
                        registry.registerBeanDefinition(beanName, beanDefinition);
                    });

        }
    }

    private Class<?> generateWsClass(Class<?> serviceIface, Object instance) {

        return null;
    }
}
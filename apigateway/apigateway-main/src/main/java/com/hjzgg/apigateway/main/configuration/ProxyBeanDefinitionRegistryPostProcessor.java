package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.soa.executor.RegisterBeanUtils;
import com.hjzgg.apigateway.soa.proxy.consumer.ConsumerDynamicProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.stream.Collectors;

public class ProxyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static Logger log = LoggerFactory.getLogger(ProxyBeanDefinitionRegistryPostProcessor.class);

    private ApplicationContext parent;

    private List<Class<?>> resourceClasses;

    private CustomAutowireConfigurer configurer = new CustomAutowireConfigurer();

    public ProxyBeanDefinitionRegistryPostProcessor(ApplicationContext parent, List<Class<?>> resourceClasses) {
        this.parent = parent;
        this.resourceClasses = resourceClasses;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        configurer.postProcessBeanFactory(beanFactory);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        /**
         * 将RequestMappingHandlerMapping 放在子上下文中, 因为其对应的api bean都在子上下文中
         * 其实现了ApplicationContextAware接口，通过setApplicationContext注入上下文，RequestMappingHandlerMapping initHandlerMethods方法中
         * 通过getApplicationContext拿到上下文，并寻找实现api接口的bean

         * 由于RequestMappingHandlerMapping的上下文是一个子上下文，即
         * @see com.hjzgg.apigateway.main.configuration.ApigatewayApplicationContext
         * 所以导致RequestMappingHandlerMapping无法获取父类上下文的controller，需要设置字段detectHandlerMethodsInAncestorContexts为true
         */
        BeanDefinitionBuilder requestMappingBuilder = BeanDefinitionBuilder.genericBeanDefinition(RequestMappingHandlerMapping.class);
        requestMappingBuilder.addPropertyValue("detectHandlerMethodsInAncestorContexts", true);
        registry.registerBeanDefinition("requestMappingHandlerMapping", requestMappingBuilder.getBeanDefinition());

        if (!CollectionUtils.isEmpty(resourceClasses)) {
            this.resourceClasses = resourceClasses.stream()
                    .filter(resourceClass -> RegisterBeanUtils.registerConsumer(registry, this.parent, ConsumerDynamicProxy.class, resourceClass))
                    .collect(Collectors.toList());
            this.printLoadResourceDetail();
        }
    }

    private void printLoadResourceDetail() {
        this.resourceClasses
                .stream()
                .collect(Collectors.groupingBy(ClassUtils::getPackageName))
                .entrySet()
                .stream()
                .forEach(entrySet -> {
                    List<Class<?>> childResourceClasses = entrySet.getValue();
                    log.debug("注册消费者 package resource " + entrySet.getKey());
                    childResourceClasses.forEach(childResourceClass -> {
                        log.debug("     >>>>" + childResourceClass.getName());
                    });
                });
    }
}

package com.hjzgg.apigateway.soa.executor.config;

import com.hjzgg.apigateway.soa.executor.RegisterBeanUtils;
import com.hjzgg.apigateway.soa.proxy.provider.ProviderDynamicProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class SOABeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static Logger log = LoggerFactory.getLogger(SOABeanDefinitionRegistryPostProcessor.class);

    private List<Class<?>> resourceClasses;

    private CustomAutowireConfigurer configurer = new CustomAutowireConfigurer();

    public SOABeanDefinitionRegistryPostProcessor(List<Class<?>> resourceClasses) {
        this.resourceClasses = resourceClasses;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        configurer.postProcessBeanFactory(beanFactory);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!CollectionUtils.isEmpty(resourceClasses)) {
            this.resourceClasses = resourceClasses.stream()
                    .filter(resourceClass -> RegisterBeanUtils.registerProvider(registry, ProviderDynamicProxy.class, resourceClass))
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
                    log.debug("registerProvider package resource " + entrySet.getKey());
                    childResourceClasses.forEach(childResourceClass -> {
                        log.debug("     >>>>" + childResourceClass.getName());
                    });
                });
    }
}

package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.beans.constants.Constants;
import com.hjzgg.apigateway.commons.utils.ApigatewayClassUtils;
import com.hjzgg.apigateway.soa.executor.ApiJarResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApigatewayApplicationContext extends AbstractRefreshableApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(ApigatewayApplicationContext.class);

    @Override
    protected void prepareRefresh() {
        //@Value注入Property Value
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setEnvironment(getEnvironment());
        addBeanFactoryPostProcessor(configurer);
        super.prepareRefresh();
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException {
        List<Class<?>> resourceClasses = this.createApiResource();
        //添加上下文BeanFactoryPostProcessor，实现路由功能，dubbo服务路径映射到springmvc requestMapping
        this.addBeanFactoryPostProcessor(new ProxyBeanDefinitionRegistryPostProcessor(this.getParent(), resourceClasses));
    }

    protected List<Class<?>> createApiResource() {
        ApiJarResourceLoader apiJarResourceLoader = new ApiJarResourceLoader(this.getClassLoader());
        List<Class<?>> resourceClasses = new ArrayList<>();
        List<String> apiJarsFilePath = this.getEnvironment().getProperty(Constants.API_JARS_FILE_PATH, List.class);
        if (!CollectionUtils.isEmpty(apiJarsFilePath)) {
            apiJarsFilePath.forEach(apiJarFilePath -> {
                try {
                    ApigatewayClassUtils.addClassPath2(apiJarFilePath, this.getClassLoader());
                    Resource resource = this.getResource(ResourceUtils.FILE_URL_PREFIX + apiJarFilePath);
                    resourceClasses.addAll(apiJarResourceLoader.loadResources(resource.getFile()));
                } catch (IOException e) {
                    log.error(String.format("load api resouce error, file path is %s", apiJarFilePath), e);
                }
            });
        }
        return resourceClasses;
    }
}

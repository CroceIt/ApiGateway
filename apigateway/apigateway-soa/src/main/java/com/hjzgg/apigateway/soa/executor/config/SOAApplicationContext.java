package com.hjzgg.apigateway.soa.executor.config;

import com.hjzgg.apigateway.beans.constants.Constants;
import com.hjzgg.apigateway.dubbo.configure.DubboAnnotationBean;
import com.hjzgg.apigateway.dubbo.configure.SelfDubboAnnotationBean;
import com.hjzgg.apigateway.dubbo.configure.SpringDubboConfig;
import com.hjzgg.apigateway.soa.executor.RegisterBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetaData;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar.BINDER_BEAN_NAME;

public class SOAApplicationContext extends AbstractRefreshableApplicationContext {
    private static final Logger log = LoggerFactory.getLogger(SOAApplicationContext.class);
    private static final String METADATA_BEAN_NAME = BINDER_BEAN_NAME + ".store";
    private List<Class<?>> resourceClasses;

    private int port;

    private SOAApplicationContext(List<Class<?>> resourceClasses, int port) {
        this.resourceClasses = resourceClasses;
        this.port = port;
    }

    @Override
    protected void prepareRefresh() {
        //@Value注入Property Value
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setEnvironment(getEnvironment());
        addBeanFactoryPostProcessor(configurer);

        ConfigurableEnvironment environment = getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> propertySourceMap = new HashMap<>();
        propertySourceMap.put("spring.dubbo.protocol.port", this.port);
        MapPropertySource mapPropertySource = new MapPropertySource(Constants.DUBBO_PROVIDER_PORT, propertySourceMap);
        propertySources.addFirst(mapPropertySource);
        super.prepareRefresh();
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        //支持 ConfigurationProperties
        BeanDefinitionBuilder meta =
                genericBeanDefinition(ConfigurationBeanFactoryMetaData.class);
        BeanDefinitionBuilder cpbppBean = genericBeanDefinition(
                ConfigurationPropertiesBindingPostProcessor.class);
        cpbppBean.addPropertyReference("beanMetaDataStore", METADATA_BEAN_NAME);
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        registry.registerBeanDefinition(BINDER_BEAN_NAME, cpbppBean.getBeanDefinition());
        registry.registerBeanDefinition(METADATA_BEAN_NAME, meta.getBeanDefinition());
        /**
         * Register all relevant annotation post processors in the given registry.
         * */
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        this.addBeanFactoryPostProcessor(new SOABeanDefinitionRegistryPostProcessor(resourceClasses));

        //注册 dubbo服务发现 相关的 bean
        Map<String, Object> properties = new HashMap<>();
        properties.put("applicationContext", this);

        log.info("注入自定义的AnnotationBean， 增加com.alibaba.dubbo.config.spring.AnnotationBean功能");
        RegisterBeanUtils.registerBean(beanFactory, SelfDubboAnnotationBean.class, "selfDubboAnnotationBean", properties, null);
        log.info("注入AnnotationBean，处理dubbo注解");
        RegisterBeanUtils.registerBean(beanFactory, DubboAnnotationBean.class, "dubboAnnotationBean", properties, null);

        //注册 dubbo 配置bean
        RegisterBeanUtils.registerBean(beanFactory, SpringDubboConfig.class, "childSpringDubboConfig", null, null);
    }

    public static void refreshSubApplicationContext(ApplicationContext applicationContext, List<Class<?>> resourceClasses, String appId, String group, int port) {
        if (CollectionUtils.isEmpty(resourceClasses)) {
            return;
        }
        String contextName = RegisterBeanUtils.registerSubApplicationContext(applicationContext, SOAApplicationContext.class, appId + "&" + group, resourceClasses, port);
        SOAConfig soaConfig = new SOAConfig();
        soaConfig.setApplicationContext(applicationContext);
        soaConfig.setPort(port);
        soaConfig.setSoaContextName(contextName);
        applicationContext.publishEvent(new SOAEvent<>(soaConfig));
    }

    public static void removeSubApplicationContext(ApplicationContext applicationContext, String appId, String group) {
        RegisterBeanUtils.removeSubApplicationContext(applicationContext, appId + "&" + group);
    }
}
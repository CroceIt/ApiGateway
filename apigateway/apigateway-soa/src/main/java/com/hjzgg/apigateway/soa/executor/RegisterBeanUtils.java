package com.hjzgg.apigateway.soa.executor;

import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.hjzgg.apigateway.commons.Constants;
import com.hjzgg.apigateway.dubbo.constant.DubboConstants;
import com.hjzgg.apigateway.soa.executor.config.SOAApplicationContext;
import com.hjzgg.apigateway.soa.proxy.ProxyFactoryBean;
import com.hjzgg.apigateway.soa.proxy.consumer.ConsumerProxyFactoryBean;
import com.hjzgg.apigateway.soa.proxy.provider.ProviderProxyFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author hujunzheng
 * @create 2018-02-03 下午10:30
 **/
public class RegisterBeanUtils {
    private static Logger log = LoggerFactory.getLogger(RegisterBeanUtils.class);
    private static BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
    public static boolean registerConsumer(BeanDefinitionRegistry registry, ApplicationContext parent, Class<?> dynamicProxyClass, Class<?> resourceClass) {
        String apiClassInfo = ClassUtils.getQualifiedName(resourceClass);
        try {
            BeanDefinitionBuilder builder = fetchBasicDubboBeanDefinitionBuilder(dynamicProxyClass, resourceClass);

            builder.addPropertyReference(DubboConstants.DUBBO_APPLICATION_CONFIG_BEAN, DubboConstants.DUBBO_APPLICATION_CONFIG_BEAN);

            if (parent.containsBeanDefinition(DubboConstants.DUBBO_MONITOR_CONFIG_BEAN)) {
                builder.addPropertyReference(DubboConstants.DUBBO_MONITOR_CONFIG_BEAN, DubboConstants.DUBBO_MONITOR_CONFIG_BEAN);
            }

            if (parent.containsBeanDefinition(DubboConstants.DUBBO_CONSUMER_CONFIG_BEAN)) {
                builder.addPropertyReference(DubboConstants.DUBBO_CONSUMER_CONFIG_BEAN, DubboConstants.DUBBO_CONSUMER_CONFIG_BEAN);
            }

            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setBeanClass(ConsumerProxyFactoryBean.class);
            beanDefinition.setAttribute(Constants.API_CLASS_INFO, resourceClass);
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            if (registry.containsBeanDefinition(beanName)) {
                log.debug(beanName + " already exists! Class is " + apiClassInfo + " .");
                return false;
            }
            registry.registerBeanDefinition(beanName, beanDefinition);
            return true;
        } catch (Exception e) {
            log.error("registerConsumer proxy bean error! Class is " + apiClassInfo + " .");
            return false;
        }
    }

    public static boolean registerProvider(BeanDefinitionRegistry registry, Class<?> dynamicProxyClass, Class<?> resourceClass) {
        String apiClassInfo = ClassUtils.getQualifiedName(resourceClass);
        try {
            /**
             * 通过代理bean的方式创建创建 服务bean
             * @see com.hjzgg.apigateway.dubbo.configure.SelfDubboAnnotationBean#postProcessAfterInitialization(Object, String)
             * if (AopUtils.isAopProxy(bean)), 判断bean的类型如果是代理类型，进行dubbo注解解析处理
             * */
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(resourceClass);
            String dubboVersion = resourceClass.getAnnotation(Service.class).version();
            String dubboGroup = resourceClass.getAnnotation(Service.class).group();
            ProviderProxyFactoryBean providerProxyFactoryBean = new ProviderProxyFactoryBean(dynamicProxyClass, resourceClass, dubboVersion, dubboGroup);
            /**
             * providerProxyFactoryBean.getObject() 得到的是通过 Proxy.newInstance方法获取到的代理类
             * @see com.hjzgg.apigateway.soa.proxy.DynamicProxyAdapter#createJDKProxy(ProxyFactoryBean, Class)
             * 可通过 Proxy.getInvocationHandler方法拿到 InvocationHandler实例
             */
            Class<?> targetProxyClass = ProviderProxyFactoryBean.getProxyClass(resourceClass);
            builder.addConstructorArgValue(Proxy.getInvocationHandler(providerProxyFactoryBean.getObject()));
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setBeanClass(targetProxyClass);

            beanDefinition.setAttribute(Constants.API_CLASS_INFO, resourceClass);
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            if (registry.containsBeanDefinition(beanName)) {
                log.debug(beanName + " already exists! Class is " + apiClassInfo + " .");
                return false;
            }
            registry.registerBeanDefinition(beanName, beanDefinition);
            return true;
        } catch (Exception e) {
            log.error("registerProvider proxy bean error! Class is " + apiClassInfo + " .");
            return false;
        }
    }

    public static String registerSubApplicationContext(ApplicationContext context, Class<?> someApplicationConetextClass, String contextName, Object... constructionArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(someApplicationConetextClass);
        if (!Objects.isNull(constructionArgs)) {
            Stream.of(constructionArgs).forEach(constructionArg -> builder.addConstructorArgValue(constructionArg));
        }
        AbstractBeanDefinition definition = builder.getBeanDefinition();
        definition.setInitMethodName("refresh");
        MutablePropertyValues propertyValues = definition.getPropertyValues();
        propertyValues.add("parent", context);
        BeanDefinitionRegistry registry = getBeanDefinitionRegistry(context);
        if (StringUtils.isBlank(contextName)) {
            contextName = beanNameGenerator.generateBeanName(definition, registry);
        }
        registry.registerBeanDefinition(contextName, definition);
        return contextName;
    }

    public static String registerBean(ApplicationContext context, Class<?> someClass, String beanName, Map<String, Object> properties, Object... constructionArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(someClass);
        if (!Objects.isNull(constructionArgs)) {
            Stream.of(constructionArgs).forEach(constructionArg -> builder.addConstructorArgValue(constructionArg));
        }
        if (!Objects.isNull(properties) && !properties.isEmpty()) {
            properties.entrySet().stream().forEach(property -> builder.addPropertyValue(property.getKey(), property.getValue()));
        }
        AbstractBeanDefinition definition = builder.getBeanDefinition();
        BeanDefinitionRegistry registry = getBeanDefinitionRegistry(context);
        if (StringUtils.isBlank(beanName)) {
            beanName = beanNameGenerator.generateBeanName(definition, registry);
        }
        registry.registerBeanDefinition(beanName, definition);
        return beanName;
    }

    public static String registerBean(BeanDefinitionRegistry registry, Class<?> someClass, String beanName, Map<String, Object> properties, Object... constructionArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(someClass);
        if (!Objects.isNull(constructionArgs)) {
            Stream.of(constructionArgs).forEach(constructionArg -> builder.addConstructorArgValue(constructionArg));
        }
        if (!(Objects.isNull(properties) || properties.isEmpty())) {
            properties.entrySet().stream().forEach(property -> builder.addPropertyValue(property.getKey(), property.getValue()));
        }
        AbstractBeanDefinition definition = builder.getBeanDefinition();
        if (StringUtils.isBlank(beanName)) {
            beanName = beanNameGenerator.generateBeanName(definition, registry);
        }
        registry.registerBeanDefinition(beanName, definition);
        return beanName;
    }

    private static BeanDefinitionBuilder fetchBasicDubboBeanDefinitionBuilder(Class<?> dynamicProxyClass, Class<?> resourceClass) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(resourceClass);
        String dubboVersion = resourceClass.getAnnotation(Service.class).version();
        String dubboGroup = resourceClass.getAnnotation(Service.class).group();

        builder.addConstructorArgValue(dynamicProxyClass)
                .addConstructorArgValue(resourceClass)//接口
                .addConstructorArgValue(dubboVersion)//version
                .addConstructorArgValue(dubboGroup);//group
        return builder;
    }

    /**
     * Get the bean definition registry.
     *
     * @param context the application context
     * @return the BeanDefinitionRegistry if it can be determined
     */
    public static BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
        if (context instanceof BeanDefinitionRegistry) {
            return (BeanDefinitionRegistry) context;
        }
        if (context instanceof AbstractApplicationContext) {
            return (BeanDefinitionRegistry) ((AbstractApplicationContext) context)
                    .getBeanFactory();
        }
        throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
    }

    public static void removeSubApplicationContext(ApplicationContext applicationContext, String contextName) {

        if (applicationContext.containsBean(contextName)) {
            SOAApplicationContext soaApplicationContext = applicationContext.getBean(contextName, SOAApplicationContext.class);
            if (soaApplicationContext.containsBean(DubboConstants.DUBBO_PROTOCOL_CONFIG_BEAN)) {
                soaApplicationContext.getBean(DubboConstants.DUBBO_PROTOCOL_CONFIG_BEAN, ProtocolConfig.class).destory();
            }
            if (soaApplicationContext.containsBean(DubboConstants.DUBBO_REGISTRY_CONFIG_BEAN)) {
                RegistryConfig registryConfig = soaApplicationContext.getBean(DubboConstants.DUBBO_REGISTRY_CONFIG_BEAN, RegistryConfig.class);
                for (Registry registry : AbstractRegistryFactory.getRegistries()) {
                    if (registry.getUrl().getAddress().equals(registryConfig.getAddress())
                            && registry.getUrl().getProtocol().equals(registryConfig.getProtocol())) {
                        registry.destroy();
                    }
                }
            }
            soaApplicationContext.destroy();
        }

        BeanDefinitionRegistry registry = getBeanDefinitionRegistry(applicationContext);
        if (registry.containsBeanDefinition(contextName)) {
            registry.removeBeanDefinition(contextName);
        }

    }
}

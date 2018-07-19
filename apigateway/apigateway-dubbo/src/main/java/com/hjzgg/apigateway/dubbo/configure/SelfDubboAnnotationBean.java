package com.hjzgg.apigateway.dubbo.configure;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @see com.alibaba.dubbo.config.spring.AnnotationBean
 */
public class SelfDubboAnnotationBean extends AbstractConfig implements DisposableBean, BeanPostProcessor, ApplicationContextAware, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Logger.class);

    private String annotationPackage;

    private String[] annotationPackages;

    private final Set<ServiceConfig<?>> serviceConfigs = new ConcurrentHashSet<>();

    private final ConcurrentMap<String, ReferenceBean<?>> referenceConfigs = new ConcurrentHashMap<>();

    public String getPackage() {
        return annotationPackage;
    }

    public void setPackage(String annotationPackage) {
        this.annotationPackage = annotationPackage;
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
                : Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
    }

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void destroy() {
        for (ServiceConfig<?> serviceConfig : serviceConfigs) {
            try {
                serviceConfig.unexport();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
        for (ReferenceConfig<?> referenceConfig : referenceConfigs.values()) {
            try {
                referenceConfig.destroy();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        if (AopUtils.isAopProxy(bean)) {
            /**
             * @see com.hjzgg.apigateway.soa.executor.RegisterBeanUtils#registerProvider
             * @see com.hjzgg.apigateway.soa.proxy.provider.ProviderDynamicProxy#invoke
             * */
            //获取被代理的真实类或者接口类
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Service service = targetClass.getAnnotation(Service.class);
            if (!Objects.isNull(service)) {
                ServiceBean<Object> serviceConfig = new ServiceBean<>(service);
                if (void.class.equals(service.interfaceClass())
                        && "".equals(service.interfaceName())) {
                    if (!(targetClass.isInterface() || targetClass.getInterfaces().length > 0)) {
                        throw new IllegalStateException("Failed to export remote service class " + bean.getClass().getName() + ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
                    } else {
                        if (targetClass.isInterface()) {
                            serviceConfig.setInterface(targetClass);
                        }

                        if (targetClass.getInterfaces().length > 0) {
                            serviceConfig.setInterface(targetClass.getInterfaces()[0]);
                        }
                    }
                }
                if (applicationContext != null) {
                    serviceConfig.setApplicationContext(applicationContext);
                    if (service.registry() != null && service.registry().length > 0) {
                        List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
                        for (String registryId : service.registry()) {
                            if (registryId != null && registryId.length() > 0) {
                                registryConfigs.add(applicationContext.getBean(registryId, RegistryConfig.class));
                            }
                        }
                        serviceConfig.setRegistries(registryConfigs);
                    }
                    if (service.provider() != null && service.provider().length() > 0) {
                        serviceConfig.setProvider(applicationContext.getBean(service.provider(), ProviderConfig.class));
                    }
                    if (service.monitor() != null && service.monitor().length() > 0) {
                        serviceConfig.setMonitor(applicationContext.getBean(service.monitor(), MonitorConfig.class));
                    }
                    if (service.application() != null && service.application().length() > 0) {
                        serviceConfig.setApplication(applicationContext.getBean(service.application(), ApplicationConfig.class));
                    }
                    if (service.module() != null && service.module().length() > 0) {
                        serviceConfig.setModule(applicationContext.getBean(service.module(), ModuleConfig.class));
                    }
                    if (service.provider() != null && service.provider().length() > 0) {
                        serviceConfig.setProvider(applicationContext.getBean(service.provider(), ProviderConfig.class));
                    }
                    if (service.protocol() != null && service.protocol().length > 0) {
                        List<ProtocolConfig> protocolConfigs = new ArrayList<ProtocolConfig>();
                        for (String protocolId : service.registry()) {
                            if (protocolId != null && protocolId.length() > 0) {
                                protocolConfigs.add(applicationContext.getBean(protocolId, ProtocolConfig.class));
                            }
                        }
                        serviceConfig.setProtocols(protocolConfigs);
                    }
                    try {
                        serviceConfig.afterPropertiesSet();
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
                serviceConfig.setRef(bean);
                serviceConfigs.add(serviceConfig);
                serviceConfig.export();
            }
        }
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    private boolean isMatchPackage(Object bean) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = bean.getClass().getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

}
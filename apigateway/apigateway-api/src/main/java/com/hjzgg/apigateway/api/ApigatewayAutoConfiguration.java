package com.hjzgg.apigateway.api;

import com.hjzgg.apigateway.api.configuration.DefaultDispatcherServlet;
import com.hjzgg.apigateway.api.configuration.DefaultRequestMappingHandlerMapping;
import com.hjzgg.apigateway.dubbo.configure.SelfDubboAnnotationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME;

@Configuration
@ComponentScan
public class ApigatewayAutoConfiguration extends DelegatingWebMvcConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ApigatewayAutoConfiguration.class);

    private static final String SPRING_DUBBO_SCAN = "spring.dubbo.scan";

    @Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet servlet = new DefaultDispatcherServlet();
        return servlet;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = new DefaultRequestMappingHandlerMapping();
        return mapping;
    }

    @Bean
    @ConfigurationProperties(SPRING_DUBBO_SCAN)
    /**
     * @see io.dubbo.springboot.DubboConfigurationApplicationContextInitializer
     * */
    public SelfDubboAnnotationBean selfDubboAnnotationBeanEnhance(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String scan = env.getProperty(SPRING_DUBBO_SCAN);
        log.info("加入自定义的AnnotationBean， 增加com.alibaba.dubbo.config.spring.AnnotationBean功能");
        SelfDubboAnnotationBean scanner = BeanUtils.instantiate(SelfDubboAnnotationBean.class);
        scanner.setPackage(scan);
        scanner.setApplicationContext(applicationContext);
        return scanner;
    }
}
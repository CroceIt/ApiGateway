package com.hjzgg.apigateway.api;

import com.hjzgg.apigateway.api.condition.ConditionalOnNotMVCApplication;
import com.hjzgg.apigateway.api.rpc.JsonRpcResolver;
import com.hjzgg.apigateway.api.rpc.JsonRpcServlet;
import com.hjzgg.apigateway.dubbo.configure.SelfDubboAnnotationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@ConditionalOnNotMVCApplication
@Configuration
@ComponentScan
public class ApigatewayAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ApigatewayAutoConfiguration.class);

    private static final String SPRING_DUBBO_SCAN = "spring.dubbo.scan";

    @Bean
    public SelfDubboAnnotationBean selfDubboAnnotationBeanEnhance(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String scan = env.getProperty(SPRING_DUBBO_SCAN);
        log.info("加入自定义的AnnotationBean， 增加com.alibaba.dubbo.config.spring.AnnotationBean功能");
        SelfDubboAnnotationBean scanner = BeanUtils.instantiate(SelfDubboAnnotationBean.class);
        scanner.setPackage(scan);
        scanner.setApplicationContext(applicationContext);
        return scanner;
    }

    @Bean
    public JsonRpcResolver jsonRpcPostResolver() {
        JsonRpcResolver resolver = new JsonRpcResolver();
        return resolver;
    }

    @Bean
    public ServletRegistrationBean jsonRpcServletRegistrationBean(JsonRpcResolver jsonRpcResolver) {
        JsonRpcServlet jsonRpcServlet = new JsonRpcServlet(jsonRpcResolver);
        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(jsonRpcServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/rpc/*");
        registrationBean.setName("jsonRpcServlet");
        return registrationBean;
    }
}
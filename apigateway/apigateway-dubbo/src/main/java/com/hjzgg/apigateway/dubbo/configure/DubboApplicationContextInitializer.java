package com.hjzgg.apigateway.dubbo.configure;

import com.hjzgg.apigateway.dubbo.constant.DubboConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author hujunzheng
 * @create 2018-07-26 15:26
 **/
public class DubboApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(DubboApplicationContextInitializer.class);

    /**
     * 参考 io.dubbo.springboot.DubboConfigurationApplicationContextInitializer
     * （在依赖 io.dubbo.springboot:spring-boot-starter-dubbo 中）
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String scan = env.getProperty(DubboConstants.SPRING_DUBBO_SCAN);
        log.info("加入dubbo服务处理的后置处理器AnnotationBean");
        DubboAnnotationBean scanner = BeanUtils.instantiate(DubboAnnotationBean.class);
        scanner.setPackage(scan);
        scanner.setApplicationContext(applicationContext);
        applicationContext.addBeanFactoryPostProcessor(scanner);
        applicationContext.getBeanFactory().registerSingleton("dubboAnnotationBean", scanner);
    }
}
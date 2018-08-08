package com.hjzgg.apigateway.datasource.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

/**
 * @author hujunzheng
 * @create 2018-08-08 13:47
 **/
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptor transactionInterceptor(PlatformTransactionManager transactionManager) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);

        Properties methods = new Properties();
        methods.put("create*", "PROPAGATION_REQUIRED,-Exception");

        interceptor.setTransactionAttributes(methods);
        return interceptor;
    }
}
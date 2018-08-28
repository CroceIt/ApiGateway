package com.hjzgg.apigateway.datasource.config;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
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
 *
 * 代理有 符合一定规则 的方法
 **/
@Configuration
@ConditionalOnProperty(prefix = "spring.mybatis.transaction", name = "config", havingValue = "method")
@ConditionalOnClass(PlatformTransactionManager.class)
@AutoConfigureAfter({ JtaAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        Neo4jDataAutoConfiguration.class })
@EnableTransactionManagement
public class MybatisTransactionWithMethodConfig extends MybatisTransactionCommonConfig{

    /**
     * 事务拦截
     * <p>在服务层进行事务拦截处理
     * @return 事务拦截器
     */
    @Bean
    public static BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
        creator.setBeanNames("*SVImpl","*ServiceImpl");
        creator.setInterceptorNames(new String[] {"txInterceptor"});
        creator.setExposeProxy(true);
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean(name = "txInterceptor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static TransactionInterceptor transactionAdvice(PlatformTransactionManager transactionManager) {
        TransactionInterceptor advice = new TransactionInterceptor();
        advice.setTransactionManager(transactionManager);
        Properties methods = new Properties();
        methods.put("add*", "PROPAGATION_REQUIRED,-Exception");
        advice.setTransactionAttributes(methods);
        return advice;
    }
}
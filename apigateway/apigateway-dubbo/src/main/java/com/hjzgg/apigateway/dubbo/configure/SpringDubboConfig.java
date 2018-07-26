package com.hjzgg.apigateway.dubbo.configure;

import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.hjzgg.apigateway.dubbo.condition.ConditionalOnPrefixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DubboConfigurationProperties.class)
//参考 io.dubbo.springboot.DubboAutoConfiguration
public class SpringDubboConfig {
    @Autowired
    private DubboConfigurationProperties dubboConfigurationProperties;

    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.application")
    public ApplicationConfig dubboApplicationConfig() {
        return dubboConfigurationProperties.getApplication();
    }

    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.consumer")
    public ConsumerConfig dubboConsumerConfig() {
        return dubboConfigurationProperties.getConsumer();
    }

    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.protocol")
    public ProtocolConfig dubboProtocolConfig() {
        return dubboConfigurationProperties.getProtocol();
    }

    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.registry")
    public RegistryConfig dubboRegistryConfig() {
        return dubboConfigurationProperties.getRegistry();
    }
    
    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.monitor")
    public MonitorConfig dubboMonitorConfig() {
        return dubboConfigurationProperties.getMonitor();
    }

    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.provider")
    public ProviderConfig dubboProviderConfig() {
        return dubboConfigurationProperties.getProvider();
    }

    @Bean
    ReferenceConfigCache dubboReferenceConfigCache() {
        return ReferenceConfigCache.getCache();
    }
}
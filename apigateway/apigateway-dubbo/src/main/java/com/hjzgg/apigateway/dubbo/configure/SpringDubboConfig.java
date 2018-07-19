package com.hjzgg.apigateway.dubbo.configure;

import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.hjzgg.apigateway.dubbo.condition.ConditionalOnPrefixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(DubboConfigurationProperties.class)
//参考 io.dubbo.springboot.DubboAutoConfiguration
public class SpringDubboConfig {
    //动态代理类需要注入如下3个bean
    /**
     * {@linkplain io.dubbo.springboot.DubboAutoConfiguration}
     * */
    public static final String DUBBO_APPLICATION_CONFIG_BEAN = "requestApplicationConfig";
    public static final String DUBBO_MONITOR_CONFIG_BEAN = "dubboMonitorConfig";
    public static final String DUBBO_CONSUMER_CONFIG_BEAN = "dubboConsumerConfig";

    public static final String DUBBO_PROTOCOL_CONFIG_BEAN = "requestProtocolConfig";
    public static final String DUBBO_REGISTRY_CONFIG_BEAN = "requestRegistryConfig";

    @Autowired
    private DubboConfigurationProperties dubboConfigurationProperties;

    @Bean
    @ConditionalOnPrefixProperty(prefix = "spring.dubbo.consumer")
    public ConsumerConfig dubboConsumerConfig() {
        return dubboConfigurationProperties.getConsumer();
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
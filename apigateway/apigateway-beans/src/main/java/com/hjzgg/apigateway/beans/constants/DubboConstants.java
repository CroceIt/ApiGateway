package com.hjzgg.apigateway.beans.constants;

/**
 * @author hujunzheng
 * @create 2018-07-26 14:40
 **/
public class DubboConstants {
    //动态代理类需要注入如z下3个bean
    public static final String DUBBO_APPLICATION_CONFIG_BEAN = "dubboApplicationConfig";
    public static final String DUBBO_MONITOR_CONFIG_BEAN = "dubboMonitorConfig";
    public static final String DUBBO_CONSUMER_CONFIG_BEAN = "dubboConsumerConfig";
    public static final String DUBBO_PROTOCOL_CONFIG_BEAN = "dubboProtocolConfig";
    public static final String DUBBO_REGISTRY_CONFIG_BEAN = "dubboRegistryConfig";


    public static final String SPRING_DUBBO_SCAN = "spring.dubbo.scan";

    public static final int SELF_DUBBO_ANNOTATION_BEAN_ORDER = Constants.LOWEST_PRECEDENCE_ORDER;
    public static final int DUBBO_ANNOTATION_BEAN_ORDER = Constants.LOWEST_PRECEDENCE_ORDER + 1;
}
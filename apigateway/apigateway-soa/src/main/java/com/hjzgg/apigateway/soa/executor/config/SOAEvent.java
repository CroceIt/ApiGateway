package com.hjzgg.apigateway.soa.executor.config;

import org.springframework.context.ApplicationEvent;

/**
 * @author hujunzheng
 * @create 2018-02-17 下午7:41
 **/
public class SOAEvent<SOAConfig> extends ApplicationEvent{
    public SOAEvent(SOAConfig source) {
        super(source);
    }
}

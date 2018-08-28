package com.hjzgg.apigateway.websocket.ifaces;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author hujunzheng
 * @create 2018-08-28 9:49
 * 获取websocket类文件
 **/
public interface IWebSocketProvide {
    void registerWebSockets(BeanDefinitionRegistry registry);
}

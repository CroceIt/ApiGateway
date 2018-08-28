package com.hjzgg.apigateway.websocket.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author hujunzheng
 * @create 2018-08-27 16:10
 **/
@Configuration
@Import(SelfWebSockRegistrar.class)
@EnableWebSocket
public class SelfWebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
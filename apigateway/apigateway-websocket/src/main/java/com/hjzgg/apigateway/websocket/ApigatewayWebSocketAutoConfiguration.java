package com.hjzgg.apigateway.websocket;

import com.hjzgg.apigateway.beans.constants.WebSocketConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = WebSocketConstants.WEB_SOCKET_AUTO_PROPERTY_KEY, havingValue = "true")
@Configuration
@ComponentScan
public class ApigatewayWebSocketAutoConfiguration {
}
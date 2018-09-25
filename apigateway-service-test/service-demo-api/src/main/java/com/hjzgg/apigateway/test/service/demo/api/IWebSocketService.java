package com.hjzgg.apigateway.test.service.demo.api;

import com.alibaba.dubbo.config.annotation.Service;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * @author hujunzheng
 * @create 2018-08-26 15:37
 **/
@ServerEndpoint(value = "/ws")
@Service(version = "iWebSocket", group = "hjzgg")
public interface IWebSocketService {
    @OnOpen
    void onOpen(Session session);

    @OnClose
    void onClose(Session session);

    @OnMessage
    void OnMessage(Session session, String message);

    @OnError
    void onError(Session session, Throwable throwable);
}

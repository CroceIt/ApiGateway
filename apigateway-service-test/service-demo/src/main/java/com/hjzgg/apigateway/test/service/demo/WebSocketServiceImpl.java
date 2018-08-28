package com.hjzgg.apigateway.websocket.test.service;

import com.hjzgg.apigateway.api.websocket.WebSocketServiceAdapter;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * @author hujunzheng
 * @create 2018-08-20 11:55
 **/
@Component
@ServerEndpoint(value = "/ws")
public class WebSocketServiceImpl extends WebSocketServiceAdapter {
    @OnOpen
    @Override
    public void onOpen(Session session) {
        System.out.println(session.getId() + " is open");
    }

    @OnClose
    @Override
    public void onClose(Session session) {
        System.out.println(session.getId() + " is close");
    }

    @OnMessage
    @Override
    public void OnMessage(Session session, String message) {
        System.out.println(session.getId() + " message is " + message);
    }

    @OnError
    @Override
    public void onError(Session session, Throwable throwable) {
        System.out.println(session.getId() + " is error, " + throwable.getMessage());
    }
}
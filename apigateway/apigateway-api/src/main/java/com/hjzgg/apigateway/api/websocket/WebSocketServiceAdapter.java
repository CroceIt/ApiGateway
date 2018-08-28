package com.hjzgg.apigateway.api.websocket;

import javax.websocket.Session;

/**
 * @author hujunzheng
 * @create 2018-08-20 11:54
 **/
public class WebSocketServiceAdapter implements IWebSocketService {
    @Override
    public void onOpen(Session session) {

    }

    @Override
    public void onClose(Session session) {

    }

    @Override
    public void OnMessage(Session session, String message) {

    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }
}
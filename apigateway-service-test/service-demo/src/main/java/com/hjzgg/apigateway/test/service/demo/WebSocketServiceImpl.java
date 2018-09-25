package com.hjzgg.apigateway.test.service.demo;

import com.alibaba.dubbo.config.annotation.Service;
import com.hjzgg.apigateway.test.service.demo.api.IWebSocketService;

import javax.websocket.*;

/**
 * @author hujunzheng
 * @create 2018-08-20 11:55
 **/
@Service
public class WebSocketServiceImpl implements IWebSocketService {
    @Override
    public void onOpen(Session session) {
        System.out.println(session.getId() + " is open");
    }

    @Override
    public void onClose(Session session) {
        System.out.println(session.getId() + " is close");
    }

    @Override
    public void OnMessage(Session session, String message) {
        System.out.println(session.getId() + " message is " + message);
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        System.out.println(session.getId() + " is error, " + throwable.getMessage());
    }
}
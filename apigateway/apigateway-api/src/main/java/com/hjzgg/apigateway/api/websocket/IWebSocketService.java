package com.hjzgg.apigateway.api.websocket;

import javax.websocket.*;

/**
 * @author hujunzheng
 * @create 2018-08-20 11:21
 **/
public interface IWebSocketService {
    void onOpen(Session session);

    void onClose(Session session);

    void OnMessage(Session session, String message);

    void onError(Session session, Throwable throwable);
}

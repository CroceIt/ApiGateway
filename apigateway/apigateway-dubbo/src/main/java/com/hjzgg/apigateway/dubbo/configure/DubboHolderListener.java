package com.hjzgg.apigateway.dubbo.configure;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

public class DubboHolderListener implements ApplicationListener {
    private static Thread holdThread;
    private static Boolean running = Boolean.FALSE;

    public void onApplicationEvent(ApplicationEvent event) {
        if ((event instanceof ApplicationPreparedEvent)) {
            if (running == Boolean.FALSE) {
                running = Boolean.TRUE;
            }
            if (holdThread == null) {
                holdThread = new Thread(() -> {
                    System.out.println(Thread.currentThread().getName());
                    while ((DubboHolderListener.running.booleanValue()) && (!Thread.currentThread().isInterrupted())) {
                        try {
                            Thread.sleep(2000L);
                        } catch (InterruptedException localInterruptedException) {
                        }
                    }
                }, "Dubbo-Holder");

                holdThread.setDaemon(false);
                holdThread.start();
            }
        }
        if ((event instanceof ContextClosedEvent)) {
            running = Boolean.FALSE;
            if (null != holdThread) {
                holdThread.interrupt();
                holdThread = null;
            }
        }
    }
}

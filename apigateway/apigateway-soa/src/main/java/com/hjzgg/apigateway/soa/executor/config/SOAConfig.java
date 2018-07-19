package com.hjzgg.apigateway.soa.executor.config;

import org.springframework.context.ApplicationContext;

/**
 * @author hujunzheng
 * @create 2018-02-17 下午8:07
 **/
public class SOAConfig {
    private ApplicationContext applicationContext;
    private String soaContextName;
    private int port;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String getSoaContextName() {
        return soaContextName;
    }

    public void setSoaContextName(String soaContextName) {
        this.soaContextName = soaContextName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

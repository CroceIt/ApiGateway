package com.hjzgg.apigateway.api.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author hujunzheng
 * @create 2018-01-05 上午10:40
 **/
public class DefaultDispatcherServlet extends DispatcherServlet {
    @Override
    protected void onRefresh(ApplicationContext context) {
    }
}

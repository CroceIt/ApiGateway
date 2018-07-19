package com.hjzgg.apigateway.main;

import com.hjzgg.apigateway.commons.Constants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author hujunzheng
 * @create 2018-01-04 下午2:27
 **/
public class ApigatewayDispatcherServlet extends DispatcherServlet {
    @Override
    protected void initStrategies(ApplicationContext context) {
        ConfigurableApplicationContext apigatewayContext = (ConfigurableApplicationContext) context.getBean(Constants.APIGATEWAY_APPLICATION_BEAN);
        super.initStrategies(apigatewayContext);
    }
}

package com.hjzgg.apigateway.api.configuration;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author hujunzheng
 * @create 2018-01-05 上午10:56
 **/
public class DefaultRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    protected void initHandlerMethods() {
        logger.info("DefaultRequestMappingHandlerMapping -> I don't want do it!");
    }
}

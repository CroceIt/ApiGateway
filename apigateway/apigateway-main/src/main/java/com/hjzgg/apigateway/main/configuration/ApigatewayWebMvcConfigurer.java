package com.hjzgg.apigateway.main.configuration;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author hujunzheng
 * @create 2018-08-20 1:32
 **/
@Component
public class ApigatewayWebMvcConfigurer extends WebMvcConfigurerAdapter {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        /**
         * 删除 MappingJackson2XmlHttpMessageConverter，保留MappingJackson2HttpMessageConverter, 防止 前者覆盖后者功能
         * 二者默认顺序参考
         * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#addDefaultHttpMessageConverters(List)
         * */
        if (CollectionUtils.isEmpty(converters)) {
            return;
        }

        int i1 = -1, i2 = -1;
        for (int i = 0; i < converters.size(); ++i) {
            if (converters.get(i) instanceof MappingJackson2XmlHttpMessageConverter) {
                i1 = i;
            } else if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                i2 = i;
            }
        }

        if (i1 == -1 || i2 == -1) {
            return;
        }
        converters.remove(i1);
    }
}
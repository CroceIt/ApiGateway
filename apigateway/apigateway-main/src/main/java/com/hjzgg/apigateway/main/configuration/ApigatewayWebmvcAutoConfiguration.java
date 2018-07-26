package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.main.ApigatewayDispatcherServlet;
import com.hjzgg.apigateway.main.resolver.JsonDataBinderArgumentResolver;
import com.hjzgg.apigateway.security.resolver.CurrentUserArgumentResolver;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME;

/**
 * @author hujunzheng
 * @create 2018-01-04 下午2:23
 **/
@Configuration
public class ApigatewayWebmvcAutoConfiguration extends DelegatingWebMvcConfiguration {
    private static final CorsConfiguration ALLOW_CORS_CONFIG = new CorsConfiguration();

    static {
        ALLOW_CORS_CONFIG.addAllowedOrigin(CorsConfiguration.ALL);
        ALLOW_CORS_CONFIG.addAllowedMethod(CorsConfiguration.ALL);
        ALLOW_CORS_CONFIG.addAllowedHeader(CorsConfiguration.ALL);
        ALLOW_CORS_CONFIG.setAllowCredentials(true);
    }

    @Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet servlet = new ApigatewayDispatcherServlet();
        servlet.setThrowExceptionIfNoHandlerFound(true);
        return servlet;
    }

    /**
     * 保证DispatcherServlet 和 RequestMappingHandlerMapping ApplicationContext一致
     * 子类上下文的RequestMappingHandlerMapping替代父类的， 定义参考
     *
     * @see com.hjzgg.apigateway.main.configuration.ProxyBeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
     * <p>
     * 这里使用默认的RequestMappingHandlerMapping，父类上下文不需要 RequestMappingHandlerMapping类型的bean
     */
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
            @Override
            protected void initHandlerMethods() {
                logger.info("DefaultRequestMappingHandlerMapping initHandlerMethods -> I don't want do it!");
            }
        };
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(new JsonDataBinderArgumentResolver());
        argumentResolvers.add(new CurrentUserArgumentResolver());
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        Map<String, CorsConfiguration> corsConfigurationMap = new HashMap<>();
        corsConfigurationMap.put("/**", ALLOW_CORS_CONFIG);
        corsConfigurationSource.setCorsConfigurations(corsConfigurationMap);
        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource);
        FilterRegistrationBean bean = new FilterRegistrationBean(corsFilter);
        bean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return bean;
    }
}

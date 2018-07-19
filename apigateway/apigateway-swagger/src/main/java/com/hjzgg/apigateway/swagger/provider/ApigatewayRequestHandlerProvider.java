package com.hjzgg.apigateway.swagger.provider;

import com.google.common.base.Function;
import com.hjzgg.apigateway.commons.Constants;
import com.hjzgg.apigateway.commons.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.RequestHandler;
import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spring.web.WebMvcRequestHandler;
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static springfox.documentation.builders.BuilderDefaults.nullToEmptyList;
import static springfox.documentation.spi.service.contexts.Orderings.byPatternsCondition;

@Component
public class ApigatewayRequestHandlerProvider implements RequestHandlerProvider {
    private final List<RequestMappingInfoHandlerMapping> handlerMappings;
    private final HandlerMethodResolver methodResolver;

    @Autowired
    public ApigatewayRequestHandlerProvider(HandlerMethodResolver methodResolver, ApplicationContext context) {
        ApplicationContext apigatewayContext = (ApplicationContext) context.getBean(Constants.APIGATEWAY_APPLICATION_BEAN);
        this.handlerMappings = ContextUtils.getBeansOfType(apigatewayContext, RequestMappingInfoHandlerMapping.class);
        this.methodResolver = methodResolver;
    }

    @Override
    public List<RequestHandler> requestHandlers() {
        return byPatternsCondition().sortedCopy(from(nullToEmptyList(handlerMappings))
                .transformAndConcat(toMappingEntries())
                .transform(toRequestHandler()));
    }

    private Function<? super RequestMappingInfoHandlerMapping,
            Iterable<Map.Entry<RequestMappingInfo, HandlerMethod>>> toMappingEntries() {
        return new Function<RequestMappingInfoHandlerMapping, Iterable<Map.Entry<RequestMappingInfo, HandlerMethod>>>() {
            @Override
            public Iterable<Map.Entry<RequestMappingInfo, HandlerMethod>> apply(RequestMappingInfoHandlerMapping input) {
                return input.getHandlerMethods().entrySet();
            }
        };
    }

    private Function<Map.Entry<RequestMappingInfo, HandlerMethod>, RequestHandler> toRequestHandler() {
        return new Function<Map.Entry<RequestMappingInfo, HandlerMethod>, RequestHandler>() {
            @Override
            public WebMvcRequestHandler apply(Map.Entry<RequestMappingInfo, HandlerMethod> input) {
                return new WebMvcRequestHandler(
                        methodResolver,
                        input.getKey(),
                        input.getValue());
            }
        };
    }
}

package com.hjzgg.apigateway.swagger.plugin;

import com.hjzgg.apigateway.commons.utils.StringConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.readers.operation.DefaultTagsProvider;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.readers.operation.SwaggerOperationTagsReader;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class SelfSwaggerOperationTagsReader extends SwaggerOperationTagsReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(SelfSwaggerOperationTagsReader.class);

    public SelfSwaggerOperationTagsReader(DefaultTagsProvider tagsProvider) {
        super(tagsProvider);
    }

    @Override
    public void apply(OperationContext context) {
        super.apply(context);
        Field tagsField = ReflectionUtils.findField(OperationBuilder.class, "tags", Set.class);
        tagsField.setAccessible(true);
        try {
            Set<String> tags = (Set<String>) tagsField.get(context.operationBuilder());
            if (!CollectionUtils.isEmpty(tags)) {
                Set<String> tagsChanged = new LinkedHashSet<>(tags.size());
                tags.forEach(tag -> tagsChanged.add(
                        StringUtils.capitalize(StringConvertor.Manipulation.separatedToCamelCase(tag, false)))
                );
                context.operationBuilder().tags(tagsChanged);
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("operation tags 修改异常", e);
        }
    }
}

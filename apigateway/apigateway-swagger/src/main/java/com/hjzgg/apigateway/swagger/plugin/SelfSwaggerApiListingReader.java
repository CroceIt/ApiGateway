package com.hjzgg.apigateway.swagger.plugin;

import com.github.javaparser.javadoc.Javadoc;
import com.google.common.base.Optional;
import com.hjzgg.apigateway.commons.utils.StringConvertor;
import com.hjzgg.apigateway.swagger.parser.CodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.swagger.web.SwaggerApiListingReader;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;

@Component
@Order(value = SWAGGER_PLUGIN_ORDER)
public class SelfSwaggerApiListingReader extends SwaggerApiListingReader {
    private final static Logger LOGGER = LoggerFactory.getLogger(SelfSwaggerApiListingReader.class);

    @Override
    public void apply(ApiListingContext apiListingContext) {
        super.apply(apiListingContext);
        Optional<? extends Class<?>> controller = apiListingContext.getResourceGroup().getControllerClass();
        if (controller.isPresent()) {
            java.util.Optional<Javadoc> javadoc = CodeRepository.getInstance().getJavaDoc(controller.get());
            javadoc.ifPresent(doc -> apiListingContext.apiListingBuilder()
                    .description(doc.getDescription().toString()));
        }
        Field tagNamesField = ReflectionUtils.findField(ApiListingBuilder.class, "tagNames");
        tagNamesField.setAccessible(true);

        try {
            Set<String> tagNames = (Set<String>) tagNamesField.get(apiListingContext.apiListingBuilder()), tagNamesChanged = new HashSet<>(tagNames.size());
            tagNames.forEach(tagName -> tagNamesChanged.add(
                    StringUtils.capitalize(StringConvertor.Manipulation.separatedToCamelCase(tagName, false))
            ));
            tagNames.clear();
            tagNames.addAll(tagNamesChanged);
        } catch (IllegalAccessException e) {
            LOGGER.error("api tagNames 修改异常", e);
        }

    }
}

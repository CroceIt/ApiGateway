package com.hjzgg.apigateway.swagger.plugin;

import com.github.javaparser.javadoc.Javadoc;
import com.hjzgg.apigateway.swagger.parser.CodeRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.readers.parameter.SwaggerExpandedParameterBuilder;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class SelfSwaggerExpandedParameterBuilder extends SwaggerExpandedParameterBuilder {
    public SelfSwaggerExpandedParameterBuilder(DescriptionResolver descriptions, EnumTypeDeterminer enumTypeDeterminer) {
        super(descriptions, enumTypeDeterminer);
    }

    @Override
    public void apply(ParameterExpansionContext context) {
        super.apply(context);
        java.util.Optional<Javadoc> javadoc = CodeRepository.getInstance().getJavaDoc(context.getField().getRawMember().getType());
        javadoc.ifPresent(doc -> context.getParameterBuilder().description(doc.getDescription().toString()));
    }
}

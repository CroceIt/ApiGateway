package com.hjzgg.apigateway.swagger.plugin;

import com.hjzgg.apigateway.swagger.annotation.SwaggerRelatedClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.plugin.core.Plugin;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.PluginRegistrySupport;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.TypeUtils;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.readers.operation.OperationModelsProvider;
import springfox.documentation.spring.web.readers.operation.OperationParameterReader;
import springfox.documentation.swagger.readers.operation.SwaggerOperationTagsReader;
import springfox.documentation.swagger.readers.parameter.SwaggerExpandedParameterBuilder;
import springfox.documentation.swagger.web.SwaggerApiListingReader;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author hujunzheng
 * @create 2018-04-02 上午11:21
 **/

@SwaggerRelatedClasses({
        OperationModelsProvider.class
        , OperationParameterReader.class
        , SwaggerApiListingReader.class
        , SwaggerExpandedParameterBuilder.class
        , SwaggerOperationTagsReader.class
})
@Component
public class DocumentationPluginsManagerPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationPluginsManagerPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (ClassUtils.isAssignableValue(DocumentationPluginsManager.class, bean)) {
            System.out.println(bean);
            List<PluginRegistry<?, ?>> pluginRegistrys = new ArrayList<>();
            Stream.of(bean.getClass().getDeclaredFields()).forEach(field -> {
                if (ClassUtils.isAssignable(field.getType(), PluginRegistry.class)) {
                    if (field.getGenericType() instanceof ParameterizedTypeImpl) {
                        ParameterizedTypeImpl type = (ParameterizedTypeImpl) field.getGenericType();
                        if (type.getActualTypeArguments().length == 2 && TypeUtils.isAssignable(DocumentationType.class, type.getActualTypeArguments()[1])) {
                            try {
                                field.setAccessible(true);
                                pluginRegistrys.add((PluginRegistry<?, ?>) field.get(bean));
                            } catch (IllegalAccessException e) {
                                LOGGER.error("DocumentationPluginsManager inner plugin {} registry process error.", field.getName() , e);
                            }
                        }
                    }
                }
            });

            pluginRegistrys.forEach(pluginRegistry -> {
                try {
                    Field pluginsField = PluginRegistrySupport.class.getDeclaredField("plugins");
                    pluginsField.setAccessible(true);
                    List<Plugin<?>> plugins = (List<Plugin<?>>) pluginsField.get(pluginRegistry), updatedPlugins = new ArrayList<>();
                    for (Plugin plugin : plugins) {
                        SwaggerRelatedClasses swaggerRelatedClasses = AnnotatedElementUtils.findMergedAnnotation(this.getClass(), SwaggerRelatedClasses.class);
                        boolean contained = false;
                        for (Class<?> clazz : swaggerRelatedClasses.value()) {
                            if (ClassUtils.getQualifiedName(clazz) == ClassUtils.getQualifiedName(plugin.getClass())) {
                                contained = true;
                                break;
                            }
                        }
                        if (!contained) {
                            updatedPlugins.add(plugin);
                        }
                    }
                    if (!CollectionUtils.isEmpty(updatedPlugins)) {
                        pluginsField.set(pluginRegistry, Collections.unmodifiableList(updatedPlugins));
                    }
                } catch (Exception e) {
                    LOGGER.error("PluginRegistry {} field 'plugins' modify error.", ClassUtils.getShortName(pluginRegistry.getClass()), e);
                }
            });
        }
        return bean;
    }
}

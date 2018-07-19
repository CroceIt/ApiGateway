package com.hjzgg.apigateway.dubbo.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * @author hujunzheng
 * @create 2018-02-26 下午3:08
 **/
public class OnPrefixPropertyCondition extends SpringBootCondition{
    private static final Logger logger = LoggerFactory.getLogger(OnPrefixPropertyCondition.class);

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        /**
         * {@link org.springframework.boot.bind.PropertiesConfigurationFactory#doBindPropertiesToTarget
         *  @link org.springframework.boot.bind.PropertiesConfigurationFactory#getPropertySourcesPropertyValues
         *  @link org.springframework.validation.DataBinder#bind(PropertyValues)}
         * */
        PropertySourcesPropertyValues propertyValues = new PropertySourcesPropertyValues(this.deducePropertySources(context.getEnvironment(), context.getBeanFactory()));
        Map<String, Object> map = metadata.getAnnotationAttributes(ConditionalOnPrefixProperty.class.getName());
        String namePrefix = (String) map.get("prefix");
        try {
            if (judgePropertyValuesContainsNamePrefix(new MutablePropertyValues(propertyValues), namePrefix)) {
                return ConditionOutcome.match("环境变量 prefix=" + namePrefix + " 匹配成功");
            } else {
                return ConditionOutcome.noMatch("环境变量 prefix=" + namePrefix + " 匹配失败");
            }
        } catch (Exception e) {
            return ConditionOutcome.noMatch("环境变量 prefix=" + namePrefix + " 匹配失败, " + e.getMessage());
        }
    }

    /**
     * {@link org.springframework.boot.bind.RelaxedDataBinder#getPropertyValuesForNamePrefix(MutablePropertyValues)} ()}
     * */
    private boolean judgePropertyValuesContainsNamePrefix(MutablePropertyValues propertyValues, String namePrefix) {
        if (!StringUtils.hasText(namePrefix)) {
            return true;
        }
        for (PropertyValue value : propertyValues.getPropertyValues()) {
            String name = value.getName();
            for (String prefix : new RelaxedNames(stripLastDot(namePrefix))) {
                for (String separator : new String[] { ".", "_" }) {
                    String candidate = (StringUtils.hasLength(prefix) ? prefix + separator
                            : prefix);
                    if (name.startsWith(candidate)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String stripLastDot(String string) {
        if (StringUtils.hasLength(string) && string.endsWith(".")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    /**
     * {@link ConfigurationPropertiesBindingPostProcessor#deducePropertySources()}
     * */
    private PropertySources deducePropertySources(Environment environment, BeanFactory beanFactory) {
        //PropertySourcesPlaceholderConfigurer是个后置处理器，condition处理过程在其前面执行，导致PropertySourcesPlaceholderConfigurer 中 appliedPropertySources为空
        //PropertySourcesPlaceholderConfigurer configurer = getSinglePropertySourcesPlaceholderConfigurer(beanFactory);
        //if (configurer != null) {
            // Flatten the sources into a single list so they can be iterated
           // return new FlatPropertySources(configurer.getAppliedPropertySources());
        //}
        if (environment instanceof ConfigurableEnvironment) {
            MutablePropertySources propertySources = ((ConfigurableEnvironment) environment)
                    .getPropertySources();
            return new FlatPropertySources(propertySources);
        }
        // empty, so not very useful, but fulfils the contract
        logger.warn("Unable to obtain PropertySources from "
                + "PropertySourcesPlaceholderConfigurer or Environment");
        return new MutablePropertySources();
    }

    private PropertySourcesPlaceholderConfigurer getSinglePropertySourcesPlaceholderConfigurer(BeanFactory beanFactory) {
        // Take care not to cause early instantiation of all FactoryBeans
        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
            Map<String, PropertySourcesPlaceholderConfigurer> beans = listableBeanFactory
                    .getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false,
                            false);
            if (beans.size() == 1) {
                return beans.values().iterator().next();
            }
            if (beans.size() > 1 && logger.isWarnEnabled()) {
                logger.warn("Multiple PropertySourcesPlaceholderConfigurer "
                        + "beans registered " + beans.keySet()
                        + ", falling back to Environment");
            }
        }
        return null;
    }

    /**
     * Convenience class to flatten out a tree of property sources without losing the
     * reference to the backing data (which can therefore be updated in the background).
     */
    private static class FlatPropertySources implements PropertySources {

        private PropertySources propertySources;

        FlatPropertySources(PropertySources propertySources) {
            this.propertySources = propertySources;
        }

        @Override
        public Iterator<PropertySource<?>> iterator() {
            MutablePropertySources result = getFlattened();
            return result.iterator();
        }

        @Override
        public boolean contains(String name) {
            return get(name) != null;
        }

        @Override
        public PropertySource<?> get(String name) {
            return getFlattened().get(name);
        }

        private MutablePropertySources getFlattened() {
            MutablePropertySources result = new MutablePropertySources();
            for (PropertySource<?> propertySource : this.propertySources) {
                flattenPropertySources(propertySource, result);
            }
            return result;
        }

        private void flattenPropertySources(PropertySource<?> propertySource,
                                            MutablePropertySources result) {
            Object source = propertySource.getSource();
            if (source instanceof ConfigurableEnvironment) {
                ConfigurableEnvironment environment = (ConfigurableEnvironment) source;
                for (PropertySource<?> childSource : environment.getPropertySources()) {
                    flattenPropertySources(childSource, result);
                }
            }
            else {
                result.addLast(propertySource);
            }
        }

    }
}

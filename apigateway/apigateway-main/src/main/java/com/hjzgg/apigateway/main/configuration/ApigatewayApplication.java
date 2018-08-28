package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.beans.constants.Constants;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author hujunzheng
 * @create 2017-12-25 下午9:51
 **/
public class ApigatewayApplication extends SpringApplication {

    public ApigatewayApplication(Object... sources) {
        super(sources);
    }

    /**
     * Get the bean definition registry.
     *
     * @param context the application context
     * @return the BeanDefinitionRegistry if it can be determined
     */
    private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
        if (context instanceof BeanDefinitionRegistry) {
            return (BeanDefinitionRegistry) context;
        }
        if (context instanceof AbstractApplicationContext) {
            return (BeanDefinitionRegistry) ((AbstractApplicationContext) context)
                    .getBeanFactory();
        }
        throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
    }

    @Override
    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        super.postProcessApplicationContext(context);
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(ApigatewayApplicationContext.class).getBeanDefinition();
        definition.setInitMethodName("refresh");
        MutablePropertyValues propertyValues = definition.getPropertyValues();
        propertyValues.add("parent", context);
        BeanDefinitionRegistry registry = getBeanDefinitionRegistry(context);
        registry.registerBeanDefinition(Constants.APIGATEWAY_APPLICATION_BEAN, definition);
    }
}

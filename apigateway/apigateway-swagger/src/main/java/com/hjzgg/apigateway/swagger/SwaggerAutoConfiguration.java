package com.hjzgg.apigateway.swagger;

import com.hjzgg.apigateway.api.annotation.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ComponentScan
@EnableSwagger2
public class SwaggerAutoConfiguration {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(CurrentUser.class)//带有此注解的参数不显示在文档中，标识的是用户实体，在鉴权完成后通过参数解析器从cookie中解析到
                .groupName("api gateway")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .apis(RequestHandlerSelectors.withClassAnnotation(RequestMapping.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("丝袜哥 构建文档")
                .description("简单优雅的restfun风格")
                .termsOfServiceUrl("http://www.cnblogs.com/hujunzheng/")
                .version("1.0")
                .build();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() throws Exception {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/document/**").addResourceLocations("classpath:/META-INF/resources/");
            }
            /*
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addRedirectViewController(path, home).setStatusCode(
                        HttpStatus.MOVED_PERMANENTLY);
            }
            */
        };
    }
}
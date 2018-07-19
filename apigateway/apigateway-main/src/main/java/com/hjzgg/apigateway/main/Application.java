package com.hjzgg.apigateway.main;

import com.hjzgg.apigateway.api.ApigatewayAutoConfiguration;
import com.hjzgg.apigateway.main.configuration.ApigatewayApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author hujunzheng
 * @create 2017-12-25 下午9:41
 **/
@SpringBootApplication(exclude = ApigatewayAutoConfiguration.class)
public class Application {
    public static void main(String[] args) {
        ApigatewayApplication apigatewayApplication = new ApigatewayApplication(Application.class);
        apigatewayApplication.run(args);
    }
}

package com.hjzgg.apigateway.datasource.config;

import com.hjzgg.apigateway.datasource.interceptor.SqlCostInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author hujunzheng
 * @create 2018-08-09 21:39
 **/
public class MybatisTransactionCommonConfig {

    @Bean
    public SqlCostInterceptor sqlCostInterceptor() {
        return new SqlCostInterceptor();
    }
}
package com.hjzgg.apigateway.soa.executor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author hujunzheng
 * @create 2018-02-17 下午7:39
 **/
@Component
public class SOAApplicationListener implements ApplicationListener<SOAEvent> {
    private static final Logger log = LoggerFactory.getLogger(SOAApplicationListener.class);

    @Override
    public void onApplicationEvent(SOAEvent event) {
        SOAConfig soaConfig = (SOAConfig) event.getSource();
        if (Objects.isNull(soaConfig)) {
            return;
        }
        ApplicationContext applicationContext = soaConfig.getApplicationContext();
        log.info("准备注册服务，applicationContext={}, port={}", soaConfig.getSoaContextName(), soaConfig.getPort());
        applicationContext.getBean(soaConfig.getSoaContextName(), SOAApplicationContext.class);
    }
}

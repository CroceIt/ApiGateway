package com.hjzgg.apigateway.websocket.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author hujunzheng
 * @create 2018-08-20 11:57
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootApplication
public class WebSocketTest {

    @Test
    public void test() {
        System.out.println("websocket start up success...");
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(WebSocketTest.class);
        application.run(args);
    }
}
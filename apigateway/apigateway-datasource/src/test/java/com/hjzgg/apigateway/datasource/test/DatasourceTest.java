package com.hjzgg.apigateway.datasource.test;

import com.hjzgg.apigateway.datasource.test.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author hujunzheng
 * @create 2018-08-08 11:33
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "spring.config.location=classpath:datasources.yml")
@SpringBootApplication
public class DatasourceTest {

//    public DatasourceTest() {
//        System.setProperty("spring.config.location", "classpath:datasources.yml");
//    }

    @Autowired
    private IUserService userService;


    @Test
    public void test() {
//        userService.createUser();
//        userService.addUser();

        System.out.println(userService.getUser());
    }
}
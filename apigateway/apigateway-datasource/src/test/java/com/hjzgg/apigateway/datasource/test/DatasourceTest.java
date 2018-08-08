package com.hjzgg.apigateway.datasource.test;

import com.hjzgg.apigateway.datasource.test.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author hujunzheng
 * @create 2018-08-08 11:33
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SpringBootApplication
public class DatasourceTest {

    @Autowired
    private IUserService userService;


    @Test
    public void test() {
        userService.createUser();
        userService.addUser();
    }
}
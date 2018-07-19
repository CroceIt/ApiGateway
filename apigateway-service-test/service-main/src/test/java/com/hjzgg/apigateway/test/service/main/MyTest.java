package com.hjzgg.apigateway.test.service.main;

import com.hjzgg.apigateway.test.service.demo.api.IHello;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MyTest {

    @Autowired
    private IHello iHello;

    @Test
    public void test() {
        iHello.test();
    }
}

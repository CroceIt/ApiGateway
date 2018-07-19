package com.hjzgg.apigateway.test.service.demo.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.hjzgg.apigateway.api.resolver.JsonContent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("hello")
@Service(version = "iHello", group = "hjzgg")
public interface IHello {
    @GetMapping("test")
    void test();

    @GetMapping("demo")
    void demo();

    @PostMapping(value = "test2", consumes = MediaType.APPLICATION_JSON_VALUE)
    String test2(@JsonContent Teacher teacher, @JsonContent Student student);

    @PostMapping(value = "test3", consumes = MediaType.APPLICATION_JSON_VALUE)
    String test3(@RequestBody User user);
}

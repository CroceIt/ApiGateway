package com.hjzgg.apigateway.test.service.demo.api;

import java.io.Serializable;

/**
 * @author hujunzheng
 * @create 2018-02-04 上午4:30
 **/
public class Teacher implements Serializable {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}

package com.hjzgg.apigateway.datasource.test.service;

import com.hjzgg.apigateway.datasource.test.model.User;

/**
 * @author hujunzheng
 * @create 2018-08-08 11:35
 **/
public interface IUserService {

    void addUser();

    void createUser();

    User getUser();
}

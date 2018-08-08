package com.hjzgg.apigateway.datasource.test.service;

import com.hjzgg.apigateway.datasource.test.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hujunzheng
 * @create 2018-08-08 11:44
 **/
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional
    @Override
    public void addUser() {
        userMapper.insert("hjzgg", "hjzgg", "18237172801");
        throw new RuntimeException("哈哈，无法创建新数据");
    }

    @Transactional
    @Override
    public void createUser() {
        userMapper.insert("qyxjj", "qyxjj", "18860233115");
    }
}
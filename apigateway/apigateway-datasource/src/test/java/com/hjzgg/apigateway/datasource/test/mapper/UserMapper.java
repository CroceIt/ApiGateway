package com.hjzgg.apigateway.datasource.test.mapper;

import com.hjzgg.apigateway.datasource.test.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author hujunzheng
 * @create 2018-08-08 12:51
 **/
@Mapper
public interface UserMapper {

    @Insert("INSERT INTO T_USER(NAME, PASSWORD, PHONE) VALUES(#{name}, #{password}, #{phone})")
    int insert(@Param("name") String name, @Param("password") String password, @Param("phone") String phone);

    @Select("SELECT * FROM T_USER WHERE id = #{id}")
    User getUser(@Param("id") String id);
}

package com.hjzgg.apigateway.test.service.demo;

import com.alibaba.dubbo.config.annotation.Service;
import com.hjzgg.apigateway.test.service.demo.api.IHello;
import com.hjzgg.apigateway.test.service.demo.api.Student;
import com.hjzgg.apigateway.test.service.demo.api.Teacher;
import com.hjzgg.apigateway.test.service.demo.api.User;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author hujunzheng
 * @create 2018-01-02 上午2:33
 **/
@Service
public class Hello implements IHello {
    @Override
    public void test() {
        System.out.println("Hello World!");
    }

    @Override
    public void demo() {
        System.out.println("This is a demo!");
    }

    @Override
    public String test2(Teacher teacher, Student student) {
        return teacher.getName() + teacher.getAge() + student.getName() + student.getAge();
    }

    @Override
    public String test3(User user) {
        System.out.println(user.getName() + user.getAge());
        return user.getName() + user.getAge();
    }

    public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException {

        InvocationHandler h = Proxy.getInvocationHandler(Hello.class.getAnnotation(Service.class));
        // 获取 AnnotationInvocationHandler 的 memberValues 字段
        Field hField = h.getClass().getDeclaredField("memberValues");
        // 因为这个字段事 private final 修饰，所以要打开权限
        hField.setAccessible(true);
        // 获取 memberValues
        Map memberValues = (Map) hField.get(h);

        Service service = Hello.class.getSuperclass().getAnnotation(Service.class);

        memberValues.put("version", service.version());
        memberValues.put("group", service.group());
    }
}

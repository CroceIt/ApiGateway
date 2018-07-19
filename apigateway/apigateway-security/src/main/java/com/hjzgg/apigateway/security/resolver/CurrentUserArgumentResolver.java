package com.hjzgg.apigateway.security.resolver;

import com.hjzgg.apigateway.api.annotation.CurrentUser;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.springframework.beans.BeanUtils.isSimpleValueType;


public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    public CurrentUserArgumentResolver() {
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        CurrentUser userAnnotation = findMethodAnnotation(CurrentUser.class, parameter);
        return null != userAnnotation;
    }

    private Object convertUserDetail(Object userDetail, MethodParameter parameter) throws ServletRequestBindingException {
        Class<?> parameterType = parameter.getParameterType();
        Class<?> userClass = ClassUtils.getUserClass(userDetail);
        if (userClass == parameterType || parameterType.isAssignableFrom(userClass)) {
            return userDetail;
        }

        CurrentUser methodAnnotation = findMethodAnnotation(CurrentUser.class, parameter);
        if (null != methodAnnotation) {
            String propertyName = methodAnnotation.value();
            if (StringUtils.hasText(propertyName)) {
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(userClass, propertyName);
                if (!parameterType.isAssignableFrom(descriptor.getPropertyType())) {
                    throw new ServletRequestBindingException("'" + propertyName + "'的类型" + descriptor.getPropertyType() + "无法赋值到" + parameterType);
                }
                try {
                    Object propertyValue = descriptor.getReadMethod().invoke(userDetail);
                    if (propertyValue == null) {
                        throw new ServletRequestBindingException(propertyName + "的值为null，无法绑定！(descriptor=" + descriptor + ")");
                    }
                    return propertyValue;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ServletRequestBindingException("无法获取用户对象属性!", e);
                }
            }
        }

        if (isSimpleValueType(parameterType) || isSimpleValueType(userClass)) {
            throw new ServletRequestBindingException("类型不一致，无法注入用户对象！source=" + userClass + ", parameter=" + parameterType);
        }

        for (Constructor<?> constructor : parameterType.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == userClass) {
                return BeanUtils.instantiateClass(constructor, userDetail);
            }
        }
        Object o = BeanUtils.instantiateClass(parameterType);
        BeanUtils.copyProperties(userDetail, o);
        return o;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Object o = BeanUtils.instantiateClass(parameter.getParameterType());
        return o;
    }

    private <T extends Annotation> T findMethodAnnotation(Class<T> annotationClass, MethodParameter parameter) {
        T annotation = parameter.getParameterAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Annotation[] annotationsToSearch = parameter.getParameterAnnotations();
        for (Annotation toSearch : annotationsToSearch) {
            annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(),
                    annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}

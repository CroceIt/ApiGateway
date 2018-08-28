package com.hjzgg.apigateway.dubbo.configure;

import com.alibaba.dubbo.config.spring.AnnotationBean;
import com.hjzgg.apigateway.beans.constants.DubboConstants;
import org.springframework.core.annotation.Order;

/**
 * @author hujunzheng
 * @create 2018-07-26 14:39
 **/
@Order(DubboConstants.DUBBO_ANNOTATION_BEAN_ORDER)
public class DubboAnnotationBean extends AnnotationBean {
}
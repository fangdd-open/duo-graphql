package com.fangdd.graphql.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于定义一个Controller的归属模块
 *
 * @author xuwenzhen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphqlModule {
    /**
     * 模块名称
     *
     * @return 模块名称
     */
    String value();
}

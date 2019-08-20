package com.fangdd.graphql.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定注册到哪个引擎中
 *
 * @author xuwenzhen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphqlEngine {
    /**
     * 指定注册到的引擎名称
     * 一个GraphQL允许有多个引擎，可指定注册到哪个引擎
     *
     * @return 引擎名称
     */
    String[] value();
}

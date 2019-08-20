package com.fangdd.graphql.provider.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 提供基础视图通过ids查询的接口
 * Created by xuwenzhen on 2019/5/12.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface IdsProvider {

}

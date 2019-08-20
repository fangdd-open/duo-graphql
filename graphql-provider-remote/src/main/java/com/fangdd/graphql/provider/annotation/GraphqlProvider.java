package com.fangdd.graphql.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标注Provider方法，被标注的字段会被注册进GraphQL引擎，用于绑定其它图
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphqlProvider {
    /**
     * 注册的Provider名称
     *
     * @return
     */
    String value();

    /**
     * 是否批量接口
     *
     * @return
     */
    boolean batch() default false;

    /**
     * 批量ID连接符，默认为半角逗号
     *
     * @return
     */
    String idSplitter() default ",";
}

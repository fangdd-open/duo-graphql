package com.fangdd.graphql.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本注解使用在Controller方法上，用于将本接口绑定到指定的视图内
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphqlExtField {
    /**
     * 需要注入到哪个位置，比如："region_Region.esfPriceTrends(id->cityId)"
     * 表示将本方法注入到Type: region_Region，成为方法priceExtend，从region_Region属性中将id映射到接口的cityId字段，未指定的可以传参
     *
     * @return
     */
    String[] value();
}

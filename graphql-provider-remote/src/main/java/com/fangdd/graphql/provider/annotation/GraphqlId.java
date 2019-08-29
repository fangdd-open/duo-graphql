package com.fangdd.graphql.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * sign as id, if your entity's id not named as 'id',then add this annotation at this id field
 * when process the ids batch request, engine will rebuild the data with the entity id
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphqlId {
}

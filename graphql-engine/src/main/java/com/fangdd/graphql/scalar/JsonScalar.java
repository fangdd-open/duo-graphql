package com.fangdd.graphql.scalar;

import graphql.Internal;

/**
 * 扩展Scalar Type，源码来自：https://github.com/graphql-java/graphql-java-extended-scalars
 *
 * @author xuwenzhen
 * @date 2019/7/16
 */
@Internal
public class JsonScalar extends ObjectScalar {
    public JsonScalar() {
        super("JSON", "any json data");
    }
}

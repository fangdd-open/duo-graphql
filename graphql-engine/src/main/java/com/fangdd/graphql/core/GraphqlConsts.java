package com.fangdd.graphql.core;

import com.fangdd.graphql.scalar.JsonScalar;

/**
 * 常量
 *
 * @author xuwenzhen
 * @date 2019/7/23
 */
public class GraphqlConsts {
    public static final String QUERY = "Query";
    public static final String MUTATION = "Mutation";
    public static final String SUBSCRIPTION = "Subscription";
    public static final String STR_SELECTIONS = "selections";
    public static final String STR_TYPENAME = "__typename";

    public static final String STR_EMPTY = "";
    public static final String STR_COMMA = ",";
    public static final String STR_AT = "@";
    public static final String STR_EXCLAMATION = "!";

    public static final String STR_CLN = ":";
    public static final String STR_XHX = "_";
    public static final String STR_DOT = ".";
    public static final String STR_M = "M_";
    public static final String STR_ID = "Id";
    public static final String STR_S = "s";
    public static final String STR_EQ = "=";
    public static final String STR_LT = "<";
    public static final String STR_GT = ">";
    public static final String STR_TURN_LINE = "\n";
    public static final String STR_MAP = "->";
    public static final String STR_IDS = "ids";
    public static final String STR_DEFAULT = "default";
    public static final String STR_NULL = "null";

    public static final String PATH_SPLITTER = "/";
    public static final String ARRAY_INDEX_START = "[";

    public static final String ARRAY_INDEX_END = "]";
    public static final char CHAR_OBJ_START = '{';
    public static final char CHAR_ARRAY_START = '[';
    public static final char CHAR_DOT = '.';
    public static final char CHAR_SYH = '"';

    public static final JsonScalar JSON_SCALAR = new JsonScalar();
}

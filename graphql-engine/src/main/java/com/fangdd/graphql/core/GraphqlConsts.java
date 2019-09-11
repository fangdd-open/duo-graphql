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
    public static final String STR_QUERY_LOWER = "query";
    public static final String MUTATION = "Mutation";
    public static final String SUBSCRIPTION = "Subscription";
    public static final String STR_SELECTIONS = "selections";
    public static final String STR_TYPENAME = "__typename";

    public static final String STR_EMPTY = "";
    public static final String STR_COMMA = ",";
    public static final String STR_SEMICOLON = ";";
    public static final String STR_AT = "@";
    public static final String STR_EXCLAMATION = "!";
    public static final String STR_STAR = "*";

    public static final String STR_CLN = ":";
    public static final String STR_XHX = "_";
    public static final String STR_DOT = ".";
    public static final String STR_M = "M_";
    public static final String STR_ID = "Id";
    public static final String STR_ID_LOWER = "id";
    public static final String STR_S = "s";
    public static final String STR_EQ = "=";
    public static final String STR_LT = "<";
    public static final String STR_GT = ">";
    public static final String STR_TURN_LINE = "\n";
    public static final String STR_MAP = "->";
    public static final String STR_IDS = "ids";
    public static final String STR_DEFAULT = "default";
    public static final String STR_APPS = "apps";
    public static final String STR_APIS = "apis";
    public static final String STR_NULL = "null";
    public static final String STR_DEFAULT_VCS_ID = "last";
    public static final String STR_SENTINEL = "sentinel";
    public static final String PATH_SPLITTER = "/";
    public static final String STR_DOUBLE_PATH_SPLITTER = "//";
    public static final String STR_SUBSCRIPTION = "subscription";

    public static final String VCS_ID = "vcsId";
    public static final String DOMAIN = "domain";
    public static final String API_CODES = "apiCodes";
    public static final int MIN_PROVIDER_API_INFO_LEN = 10;

    public static final String ARRAY_INDEX_START = "[";
    public static final String ARRAY_INDEX_END = "]";
    public static final char CHAR_OBJ_START = '{';
    public static final char CHAR_ARRAY_START = '[';
    public static final char CHAR_DOT = '.';

    public static final char CHAR_SYH = '"';
    public static final JsonScalar JSON_SCALAR = new JsonScalar();
    public static final String STR_START = "start";
    public static final String STR_VARIABLES = "variables";
    public static final String STR_OPERATION_NAME = "operationName";
    public static final String SUBPROTOCOLS = "graphql-ws";
    public static final String STR_SUB = "sub";
}

package com.fangdd.graphql.core.util;

import com.fangdd.graphql.core.GraphqlConsts;
import com.fangdd.graphql.core.exception.GraphqlBuildException;
import com.fangdd.graphql.core.exception.GraphqlInvocationException;
import com.fangdd.graphql.pipeline.RegistryState;
import com.fangdd.graphql.provider.dto.provider.Entity;
import com.fangdd.graphql.provider.dto.provider.EntityRef;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import graphql.Scalars;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static graphql.Scalars.*;

/**
 * @author xuwenzhen
 * @date 2019/4/29
 */
public class GraphqlTypeUtils {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlTypeUtils.class);

    public static final String PATH_VARIABLE = "@PathVariable";
    public static final String REQUEST_BODY = "@RequestBody";

    private static final Map<String, GraphQLType> GRAPHQL_TYPE_MAP = Maps.newHashMap();

    static {
        GRAPHQL_TYPE_MAP.put("String", Scalars.GraphQLString);
        GRAPHQL_TYPE_MAP.put(String.class.getName(), Scalars.GraphQLString);

        GRAPHQL_TYPE_MAP.put("boolean", GraphQLBoolean);
        GRAPHQL_TYPE_MAP.put("Boolean", GraphQLBoolean);
        GRAPHQL_TYPE_MAP.put(Boolean.class.getName(), GraphQLBoolean);

        GRAPHQL_TYPE_MAP.put("int", GraphQLInt);
        GRAPHQL_TYPE_MAP.put("Integer", GraphQLInt);
        GRAPHQL_TYPE_MAP.put(Integer.class.getName(), GraphQLInt);

        GRAPHQL_TYPE_MAP.put("flat", GraphQLFloat);
        GRAPHQL_TYPE_MAP.put("Float", GraphQLFloat);
        GRAPHQL_TYPE_MAP.put(Float.class.getName(), GraphQLFloat);

        GRAPHQL_TYPE_MAP.put("double", GraphQLFloat);
        GRAPHQL_TYPE_MAP.put("Double", GraphQLFloat);
        GRAPHQL_TYPE_MAP.put(Double.class.getName(), GraphQLFloat);

        GRAPHQL_TYPE_MAP.put("long", GraphQLLong);
        GRAPHQL_TYPE_MAP.put("Long", GraphQLLong);
        GRAPHQL_TYPE_MAP.put(Long.class.getName(), GraphQLLong);

        GRAPHQL_TYPE_MAP.put("short", GraphQLShort);
        GRAPHQL_TYPE_MAP.put("Short", GraphQLShort);
        GRAPHQL_TYPE_MAP.put(Short.class.getName(), GraphQLShort);

        GRAPHQL_TYPE_MAP.put("byte", GraphQLByte);
        GRAPHQL_TYPE_MAP.put("Byte", GraphQLByte);
        GRAPHQL_TYPE_MAP.put(Byte.class.getName(), GraphQLByte);

        GRAPHQL_TYPE_MAP.put("float", GraphQLFloat);
        GRAPHQL_TYPE_MAP.put("Float", GraphQLFloat);
        GRAPHQL_TYPE_MAP.put(Float.class.getName(), GraphQLFloat);

        GRAPHQL_TYPE_MAP.put("BigDecimal", GraphQLBigDecimal);
        GRAPHQL_TYPE_MAP.put(BigDecimal.class.getName(), GraphQLBigDecimal);

        GRAPHQL_TYPE_MAP.put("Date", GraphQLLong);
        GRAPHQL_TYPE_MAP.put(Date.class.getName(), GraphQLLong);
    }

    public static GraphQLType getBaseGraphQLType(String typeName, String fieldName) {
        GraphQLType graphQLType = GRAPHQL_TYPE_MAP.get(typeName);
//        if (graphQLType != null && GraphqlConsts.STR_ID_LOWER.equals(fieldName)) {
//            // ID
//            return GraphQLID;
//        }
        return graphQLType;
    }

    private GraphqlTypeUtils() {
    }

    public static String getFieldTypeName(GraphQLType fieldType) {
        if (GraphQLList.class.isInstance(fieldType)) {
            return getFieldTypeName(((GraphQLList) fieldType).getWrappedType());
        }
        return fieldType.getName();
    }

    public static GraphQLOutputType getFieldGraphQLOutputType(GraphQLOutputType type, String fieldName) {
        if (GraphQLObjectType.class.isInstance(type)) {
            GraphQLFieldDefinition fieldDefinition = ((GraphQLObjectType) type).getFieldDefinition(fieldName);
            if (fieldDefinition == null) {
                throw new GraphqlInvocationException("GraphQLObjectType：" + type.getName() + "，中不存在字段：" + fieldName);
            }
            return fieldDefinition.getType();
        } else if (GraphQLList.class.isInstance(type)) {
            return getFieldGraphQLOutputType((GraphQLOutputType) ((GraphQLList) type).getWrappedType(), fieldName);
        }
        throw new GraphqlInvocationException(type.getName() + "无法找到字段：" + fieldName);
    }

    public static GraphQLType getGraphqlInputType(RegistryState registryState, String moduleName, String entityName) {
        GraphQLType baseGraphQLType = GraphqlTypeUtils.getBaseGraphQLType(entityName, null);
        if (baseGraphQLType != null) {
            return baseGraphQLType;
        }
        Entity entity = registryState.getEntity(entityName);
        if (entity == null) {
            logger.warn("找不到对应的类型：{}", entityName);
            return null;
        }

        Boolean map = entity.getMap();
        if (map != null && map) {
            // Map
            throw new GraphqlBuildException("不支持Map类型参数，忽略！");
        }
        Boolean collection = entity.getCollection();
        if (collection != null && collection) {
            // 列表
            if (CollectionUtils.isEmpty(entity.getParameteredEntityRefs())) {
                throw new GraphqlBuildException("泛型类型未指定:" + entityName);
            }
            EntityRef parameteredEntityRef = entity.getParameteredEntityRefs().get(0);
            GraphQLType paramGraphQLType = getGraphqlInputType(registryState, moduleName, parameteredEntityRef.getEntityName());
            if (paramGraphQLType == null) {
                throw new GraphqlBuildException("无法转换泛型类型:" + entityName);
            }
            return addAndGetGraphQLListType(paramGraphQLType);
        }

        boolean isEnum = entity.getEnumerate() != null && entity.getEnumerate();
        if (isEnum) {
            //如果是枚举
            return addAndGetGraphQLEnumType(registryState, moduleName, entity);
        }

        GraphQLType graphQLType = registryState.getGraphQLType(entityName);
        if (graphQLType != null) {
            return graphQLType;
        }

        //Pojo
        if (CollectionUtils.isEmpty(entity.getFields())) {
            throw new GraphqlBuildException("实体没有属性: " + entityName);
        }

        return addAndGetInputGraphQLType(registryState, moduleName, entity);
    }

    /**
     * create enum type
     */
    public static GraphQLEnumType addAndGetGraphQLEnumType(RegistryState registryState, String moduleName, Entity entity) {
        String name = GraphqlTypeUtils.getModuleTypeName(entity, moduleName);
        GraphQLType type = registryState.getGraphQLType(name);

        if (type != null) {
            return (GraphQLEnumType) type;
        }
        if (CollectionUtils.isEmpty(entity.getFields())) {
            throw new GraphqlBuildException("枚举:" + entity.getName() + ",元素为空！");
        }

        GraphQLEnumType.Builder enumTypeBuilder = GraphQLEnumType.newEnum().name(name);
        if (!StringUtils.isEmpty(entity.getComment())) {
            enumTypeBuilder.description(entity.getComment());
        }

        entity.getFields().forEach(field -> {
            GraphQLEnumValueDefinition.Builder enumValueBuild =
                    GraphQLEnumValueDefinition.newEnumValueDefinition().name(field.getName());
            if (!StringUtils.isEmpty(field.getComment())) {
                enumValueBuild.description(field.getComment());
            }
            enumValueBuild.value(field.getName());
            enumTypeBuilder.value(enumValueBuild.build());
        });
        GraphQLEnumType graphQLEnumType = enumTypeBuilder.build();
        registryState.addGraphQLType(name, graphQLEnumType);
        return graphQLEnumType;
    }

    private static GraphQLType addAndGetInputGraphQLType(RegistryState registryState, String moduleName, Entity entity) {
        if (CollectionUtils.isEmpty(entity.getFields())) {
            throw new GraphqlBuildException("Pojo：" + entity.getName() + "，无属性！");
        }

        String name = getModuleTypeName(entity, moduleName);
        List<GraphQLInputObjectField> graphQLFieldDefinitions = Lists.newArrayList();
        entity.getFields().forEach(field -> {
            GraphQLInputType fieldType = (GraphQLInputType) getGraphqlInputType(registryState, moduleName, field.getEntityName());
            if (fieldType == null) {
                if (entity.getEnumerate() != null && entity.getEnumerate()) {
                    //如果是枚举

                }
                return;
            }
            String fieldName = field.getName();

            GraphQLInputObjectField.Builder fieldDefinition = GraphQLInputObjectField.newInputObjectField()
                    .name(fieldName)
                    .type(fieldType);
            if (!StringUtils.isEmpty(field.getComment())) {
                fieldDefinition.description(field.getComment());
            }
            graphQLFieldDefinitions.add(fieldDefinition.build());
        });

        GraphQLInputObjectType.Builder objectTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(name)
                .fields(graphQLFieldDefinitions);

        if (!StringUtils.isEmpty(entity.getComment())) {
            objectTypeBuilder.description(entity.getComment());
        }

        GraphQLType graphQLType = objectTypeBuilder.build();
        registryState.addGraphQLType(entity.getName(), graphQLType);
        return graphQLType;
    }

    private static GraphQLList addAndGetGraphQLListType(GraphQLType graphqlType) {
        return GraphQLList.list(graphqlType);
    }


    public static String getModuleTypeName(Entity entity, String moduleName) {
        return getModuleTypeName(moduleName, getSimpleName(entity.getName()));
    }

    public static String getModuleTypeName(String groupName, String simpleEntityName) {
        return groupName + GraphqlConsts.STR_XHX + simpleEntityName;
    }

    private static String getSimpleName(String name) {
        if (!name.contains(GraphqlConsts.STR_LT)) {
            int index = name.lastIndexOf(GraphqlConsts.CHAR_DOT);
            String simpleName = name.substring(index + 1);
            if (simpleName.contains(GraphqlConsts.STR_GT)) {
                simpleName = simpleName.replaceAll(GraphqlConsts.STR_GT, GraphqlConsts.STR_EMPTY);
            }
            return simpleName;
        }
        String[] names = name.split(GraphqlConsts.STR_LT);

        StringBuilder sb = new StringBuilder();
        for (String name1 : names) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(getSimpleName(name1));
        }
        return sb.toString();
    }
}

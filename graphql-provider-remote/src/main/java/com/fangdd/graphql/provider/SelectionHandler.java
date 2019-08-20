package com.fangdd.graphql.provider;

import com.fangdd.graphql.provider.annotation.GraphqlAlias;
import com.fangdd.graphql.provider.annotation.GraphqlField;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xuwenzhen
 * @date 2019/5/20
 */
public class SelectionHandler {
    private static final ThreadLocal<List<String>> THREAD_LOCAL_SELECTIONS = new ThreadLocal<>();

    private static final Map<String, PojoSelection> TYPE_POJO_SELECTION_MAP = new ConcurrentHashMap<>();
    private static final String SELECTION_DOT = ".";
    private static final String BASE_PACKAGE = "java.";
    private static final String SELECTION_SPLITTER = "!";

    private static final Pattern GENERIC_PATTERN = Pattern.compile("^.*<(.*)>$");
    private static final int SELECTION_INDEX_START = 2;

    public static List<String> getSelections() {
        return THREAD_LOCAL_SELECTIONS.get();
    }

    private SelectionHandler() {
    }

    /**
     * 分析接口响应类型
     *
     * @param method 接口方法
     */
    public static PojoSelection analyseTypeMapping(Method method) {
        if (ErrorController.class.isAssignableFrom(method.getDeclaringClass())) {
            //如果是SpringMVC的错误处理类，则不需要再做处理
            return null;
        }
        return analyseType(method.getGenericReturnType());
    }

    public static List<String> getSelections(PojoSelection pojoSelection, String selections, String prefix) {
        List<String> renamedSelections = new ArrayList<>();
        String[] selectionArray = selections.split(SELECTION_SPLITTER);
        if (selectionArray.length == 0) {
            return new ArrayList<>();
        }
        if (prefix != null && !prefix.endsWith(SELECTION_DOT)) {
            prefix += SELECTION_DOT;
        }

        //链
        LinkedList<PojoSelection> selectionList = new LinkedList<>();
        LinkedList<String> selectionParentList = new LinkedList<>();
        selectionList.add(pojoSelection);
        //当前的PojoSelection
        PojoSelection currentPojoSelection = pojoSelection;
        String currentSelectionParent = "";
        for (String selection : selectionArray) {
            int index = selection.lastIndexOf(SELECTION_DOT);
            if (index > -1) {
                selection = selection.substring(index + 1);
            }
            while (selectionList.size() > index + SELECTION_INDEX_START) {
                //需要推出一部分
                selectionList.pollLast();
                selectionParentList.pollLast();
                currentPojoSelection = selectionList.getLast();
                if (selectionParentList.isEmpty()) {
                    currentSelectionParent = "";
                } else {
                    currentSelectionParent = selectionParentList.getLast();
                }
            }

            String fieldName = currentPojoSelection.getFieldName(selection);

            PojoSelection fieldSelection = currentPojoSelection.getSelection(selection);
            if (fieldSelection != null) {
                selectionList.add(fieldSelection);
                currentPojoSelection = fieldSelection;
                if (currentSelectionParent.length() == 0) {
                    currentSelectionParent = fieldName;
                } else {
                    currentSelectionParent += (SELECTION_DOT + fieldName);
                }
                selectionParentList.add(currentSelectionParent);
            } else {
                String newFieldName = currentSelectionParent.length() == 0 ? fieldName : currentSelectionParent + SELECTION_DOT + fieldName;
                if (!StringUtils.isEmpty(prefix) && newFieldName.startsWith(prefix)) {
                    newFieldName = newFieldName.substring(prefix.length());
                }
                if (newFieldName.contains(SELECTION_SPLITTER)) {
                    String[] dependencies = newFieldName.split(SELECTION_SPLITTER);
                    for (int i = 0; i < dependencies.length; i++) {
                        String nn = dependencies[i];
                        if (i == 0) {
                            renamedSelections.add(nn);
                        } else {
                            renamedSelections.add(currentPojoSelection.getFieldName(nn));
                        }
                    }
                } else {
                    renamedSelections.add(newFieldName);
                }
            }
        }
        return renamedSelections;
    }

    private static PojoSelection analyseType(Type type) {
        PojoSelection pojoSelection = TYPE_POJO_SELECTION_MAP.get(type);
        if (pojoSelection != null) {
            return pojoSelection;
        }

        Class clazz;
        boolean isCollection = false;
        Map<String, Class> genericMap = null;
        if (type instanceof ParameterizedType) {
            Class rawTypeClass = (Class) ((ParameterizedType) type).getRawType();
            if (Iterable.class.isAssignableFrom(rawTypeClass)) {
                //如果是集合
                clazz = getListParameterizedClass((ParameterizedType) type, null);
                if (clazz == null) {
                    throw new GraphqlSelectionException("不支持Map类型Selection");
                }
                isCollection = true;
            } else if (Map.class.isAssignableFrom(rawTypeClass)) {
                //如果是Map
                throw new GraphqlSelectionException("不支持Map类型Selection");
            } else {
                genericMap = getGenericMap(type);
                //如果是其它，包含泛型的类
                clazz = rawTypeClass;
            }
        } else {
            //如果是普通类型（无泛型）
            clazz = (Class) type;
        }
        pojoSelection = analyseClass(clazz, genericMap);
        if (isCollection && pojoSelection != null) {
            PojoSelection listPojoSelection = new PojoSelection(type.getTypeName());
            listPojoSelection.merge(pojoSelection);
            TYPE_POJO_SELECTION_MAP.put(type.getTypeName(), listPojoSelection);
        }
        return pojoSelection;
    }

    /**
     * 获取列表的泛型参数类型
     *
     * @param listType   列表类型
     * @param genericMap 列表类型泛型定义与泛型实体Map
     * @return
     */
    private static Class getListParameterizedClass(ParameterizedType listType, Map<String, Class> genericMap) {
        Type type = listType.getActualTypeArguments()[0];
        if (type instanceof TypeVariable) {
            if (genericMap == null) {
                throw new GraphqlSelectionException("无确认泛型类型：" + listType.toString() + "的泛型类型");
            }
            return genericMap.get(((TypeVariable) type).getName());
        }
        return (Class) type;
    }

    /**
     * 获取响应体的泛型映射关系，比如：T=>House
     *
     * @param type 类型
     * @return 泛型映射关系，如果出错，会抛出 GraphqlSelectionException 异常
     */
    private static Map<String, Class> getGenericMap(Type type) {
        if (!(type instanceof ParameterizedType)) {
            //如果不是泛型类型
            return new HashMap<>(0);
        }

        Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
        if (arguments.length == 0) {
            //这里出现了问题
            throw new GraphqlSelectionException("类型：" + type + "未明确声明泛型类型！");
        }
        Type rawType = ((ParameterizedType) type).getRawType();
        if (!(rawType instanceof Class)) {
            throw new GraphqlSelectionException("类型：" + type + "未明确声明异常！");
        }

        String genericString = ((Class) rawType).toGenericString();
        Matcher matcher = GENERIC_PATTERN.matcher(genericString);
        if (!matcher.find()) {
            throw new GraphqlSelectionException("类型：" + type + "响应体无法找到泛型定义:" + genericString);
        }
        String generics = matcher.group(1);
        String[] gs = generics.split(",");
        if (gs.length != arguments.length) {
            //出问题了！
            throw new GraphqlSelectionException("类型：" + type + "响应体泛型数量异常！定义数量：" + gs.length + ",实现数量：" + arguments.length);
        }

        Map<String, Class> genericMap = new HashMap<>(16);
        for (int i = 0; i < gs.length; i++) {
            String generic = gs[i];
            if (ParameterizedType.class.isInstance(arguments[i])) {
                Type argument = getListParameterizedClass((ParameterizedType) arguments[i], null);
                genericMap.put(generic, (Class) argument);
            } else {
                Class<?> clazz = (Class<?>) arguments[i];
                genericMap.put(generic, clazz);
            }
        }
        return genericMap;
    }

    private static PojoSelection analyseClass(Class<?> clazz, Map<String, Class> genericMap) {
        if (clazz.getName().startsWith(BASE_PACKAGE) || clazz.isPrimitive()) {
            //基本类型
            return null;
        }

        String key = getKey(clazz, genericMap);

        PojoSelection pojoSelection = TYPE_POJO_SELECTION_MAP.get(key);
        if (pojoSelection != null) {
            return pojoSelection;
        }
        pojoSelection = new PojoSelection(clazz.getName());
        TYPE_POJO_SELECTION_MAP.put(key, pojoSelection);

        Field[] fs = clazz.getDeclaredFields();
        for (Field field : fs) {
            Class<?> fieldType = field.getType();
            PojoSelection fieldSelection = null;
            if (Collection.class.isAssignableFrom(fieldType)) {
                //集合
                Class argumentClass = getListParameterizedClass((ParameterizedType) field.getGenericType(), genericMap);
                fieldSelection = analyseClass(argumentClass, genericMap);
            } else if (!Map.class.isAssignableFrom(fieldType)) {
                fieldSelection = analyseClass(fieldType, genericMap);
            }
            if (fieldSelection != null) {
                pojoSelection.addField(field.getName(), fieldSelection);
                if (fieldSelection.isNotEmpty()) {
                    pojoSelection.addMapping(field.getName(), field.getName());
                }
            }

            analyseField(field, pojoSelection);
        }

        Class superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            PojoSelection superPojoSelection = analyseClass(superClass, genericMap);
            if (superPojoSelection != null && superPojoSelection.isNotEmpty()) {
                pojoSelection.merge(superPojoSelection);
            }
        }
        return pojoSelection;
    }

    private static String getKey(Class<?> clazz, Map<String, Class> genericMap) {
        if (CollectionUtils.isEmpty(genericMap)) {
            return clazz.getName();
        }
        StringBuilder sb = new StringBuilder(clazz.getName()).append("<");
        genericMap.entrySet().forEach(entry -> {
            sb.append(entry.getValue().getTypeName());
            sb.append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        sb.append(">");
        return sb.toString();
    }

    private static void analyseField(Field field, PojoSelection pojoSelection) {
        JsonAlias alias = field.getAnnotation(JsonAlias.class);
        String fieldName = field.getName();
        String newFieldName = fieldName;
        if (alias != null && alias.value().length > 0) {
            newFieldName = alias.value()[0];
            pojoSelection.addMapping(fieldName, newFieldName);
        }
        GraphqlAlias graphqlAlias = field.getAnnotation(GraphqlAlias.class);
        if (graphqlAlias != null) {
            newFieldName = graphqlAlias.value();
            pojoSelection.addMapping(fieldName, newFieldName);
        }

        GraphqlField graphqlField = field.getAnnotation(GraphqlField.class);
        if (graphqlField == null) {
            return;
        }
        if (graphqlField.value() != Object.class || !StringUtils.isEmpty(graphqlField.controller())) {
            //定义了Controller
            return;
        }
        String[] fields = graphqlField.dependency();
        if (fields.length == 0) {
            return;
        }
        List<String> aliasNames = new ArrayList<>();
        aliasNames.add(newFieldName);
        for (String dependencyName : fields) {
            if (!StringUtils.isEmpty(dependencyName) && !aliasNames.contains(dependencyName)) {
                aliasNames.add(dependencyName);
            }
        }

        pojoSelection.addMapping(fieldName, String.join(SELECTION_SPLITTER, aliasNames));
    }


    public static void set(List<String> realSelections) {
        THREAD_LOCAL_SELECTIONS.set(realSelections);
    }

    public static void clear() {
        THREAD_LOCAL_SELECTIONS.remove();
    }

    public static String print() {
        StringBuilder sb = new StringBuilder();
        TYPE_POJO_SELECTION_MAP.entrySet().forEach(entry -> {
            sb.append(entry.getValue().toString());
            sb.append("\n");
        });
        return sb.toString();
    }
}

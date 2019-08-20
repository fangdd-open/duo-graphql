package com.fangdd.graphql.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuwenzhen
 * @date 2019/7/1
 */
public class PojoSelection {
    private String className;

    private Map<String, String> fieldMap = new HashMap<>(16);
    private Map<String, PojoSelection> fieldSelectionMap = new HashMap<>(16);

    public PojoSelection(String className) {
        this.className = className;
    }

    public void addMapping(String fieldName, String alias) {
        fieldMap.put(fieldName, alias);
    }

    public boolean isNotEmpty() {
        return !fieldMap.isEmpty();
    }

    public void merge(PojoSelection pojoSelection) {
        if (pojoSelection == null || pojoSelection.fieldMap.isEmpty()) {
            return;
        }
        fieldMap.putAll(pojoSelection.fieldMap);
    }

    public String getFieldName(String selection) {
        return fieldMap.getOrDefault(selection, selection);
    }

    public void addField(String fieldName, PojoSelection fieldSelection) {
        fieldSelectionMap.put(fieldName, fieldSelection);
    }

    public PojoSelection getSelection(String fieldName) {
        return fieldSelectionMap.get(fieldName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(className);
        sb.append(": {");
        if (isNotEmpty()) {
            fieldMap.entrySet().forEach(entry -> {
                sb.append("\n\t\"");
                String fieldName = entry.getKey();
                sb.append(fieldName);
                sb.append("\": ");
                sb.append("\"");
                sb.append(entry.getValue());
                sb.append("\",");
            });
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("\n}");
        return sb.toString();
    }
}

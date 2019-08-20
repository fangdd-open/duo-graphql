package com.fangdd.graphql.core;

import com.fangdd.graphql.core.util.GraphqlContextUtils;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import graphql.ExecutionInput;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.function.Function;

/**
 * 基于Caffeine Cache的请求缓存处理类
 *
 * @author xuwenzhen
 * @date 2019/8/1
 */
@Service
public class CaffeinePreparsedDocumentProvider implements PreparsedDocumentProvider {
    @Autowired(required = false)
    private ExecutionMonitor executionMonitor;

    /**
     * This is called to get a "cached" pre-parsed query and if its not present, then the computeFunction
     * can be called to parse and validate the query
     *
     * @param executionInput  The {@link ExecutionInput} containing the query
     * @param computeFunction If the query has not be pre-parsed, this function can be called to parse it
     * @return an instance of {@link PreparsedDocumentEntry}
     */
    @Override
    public PreparsedDocumentEntry getDocument(ExecutionInput executionInput, Function<ExecutionInput, PreparsedDocumentEntry> computeFunction) {
        if (StringUtils.isEmpty(executionInput.getOperationName())) {
            return computeFunction.apply(executionInput);
        }
        UserExecutionContext context = (UserExecutionContext) executionInput.getContext();
        String key = Hashing.goodFastHash(32).hashString(executionInput.getQuery(), Charsets.UTF_8).toString();
        context.setExecutionKey(key);
        return (PreparsedDocumentEntry) GraphqlContextUtils.CACHE.get(key, k -> {
            if (executionMonitor != null) {
                executionMonitor.createExecutionCache(k, executionInput);
            }
            return computeFunction.apply(executionInput);
        });
    }
}

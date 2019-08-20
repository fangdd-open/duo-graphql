package com.fangdd.graphql.provider;

import com.fangdd.graphql.provider.annotation.GraphqlSelection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xuwenzhen
 * @date 2019/5/20
 */
@Configuration
@ConditionalOnWebApplication
public class GraphqlProviderMvcInterceptorConfigurer implements WebMvcConfigurer {
    private static final String SELECTIONS = "selections";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String selections = request.getParameter(SELECTIONS);
                if (!StringUtils.isEmpty(selections) && HandlerMethod.class.isInstance(handler)) {
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
                    Method method = handlerMethod.getMethod();
                    GraphqlSelection graphqlSelection = method.getAnnotation(GraphqlSelection.class);
                    String prefix = null;
                    if (graphqlSelection != null) {
                        prefix = graphqlSelection.value();
                    }
                    PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
                    if (pojoSelection == null) {
                        return false;
                    }
                    List<String> realSelections = SelectionHandler.getSelections(pojoSelection, selections, prefix);
                    SelectionHandler.set(realSelections);
                }
                return true;
            }

            @Override
            public void afterCompletion(
                    HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex
            ) throws Exception {
                SelectionHandler.clear();
            }
        }).addPathPatterns("/**");
    }
}

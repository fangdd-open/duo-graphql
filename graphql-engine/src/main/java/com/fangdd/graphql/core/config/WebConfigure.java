package com.fangdd.graphql.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author xuwenzhen
 * @date 2019/4/14
 */
@Configuration
public class WebConfigure implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = getObjectMapper();
        converter.setObjectMapper(mapper);
        converters.add(converter);
    }

    @Bean
    @Primary
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                //反序列化时，忽略目标对象没有的属性
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                //下面配置是值为null时，不显示
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)

                //下面一个配置是集合返回为空时，不显示
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                ;
    }{

    }
}
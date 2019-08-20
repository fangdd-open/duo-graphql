package com.fangdd.graphql.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author xuwenzhen
 * @date 2019/4/4
 */
@Component
public class ProviderConfig {
    private static final Logger logger = LoggerFactory.getLogger(ProviderConfig.class);

    @PostConstruct
    public void scanGraphqlProvider() {

    }
}

package com.duoec.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author xuwenzhen
 */
@EnableWebMvc
@SpringBootApplication(scanBasePackages = "com.duoec") //Spring启动时需要扫描的包名
public class GraphqlProviderApplication {
    public static void main(String[] args) {
        new SpringApplication(GraphqlProviderApplication.class).run(args);
    }
}
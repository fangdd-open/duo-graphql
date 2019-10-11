package com.duoec.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication(scanBasePackages = "com.fangdd") //Spring启动时需要扫描的包名
public class GraphqlEngineApplication {
    public static void main(String[] args) {
        new SpringApplication(GraphqlEngineApplication.class).run(args);
    }
}
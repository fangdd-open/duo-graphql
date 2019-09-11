package com.fangdd.jedis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author xuwenzhen
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.fangdd")
public class Application {
    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}

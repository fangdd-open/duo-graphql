package com.fangdd.graphql.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.sockjs.transport.handler.SockJsWebSocketHandler;

/**
 * 开启WebSocket支持
 *
 * @author xuwenzhen
 * @date 2019/8/8
 */
@Configuration
public class WebSocketConfig {
//    @Bean
//    public SockJsWebSocketHandler getGraphqlWs() {
//        return new SockJsWebSocketHandler() {
//
//        };
//    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter() {
            @Override
            public void afterPropertiesSet() {
                super.afterPropertiesSet();

            }
        };
    }
}

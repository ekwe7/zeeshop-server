package com.ekwe_hub.zeeshopserver.common.config;

import org.springframework.context.annotation.Configuration;

@Configuration
@EnableWebSocketMessageBroker
public class webSocketConfig implements webSocketMessageBrokerConfigurer{

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrfix("/app");

        /// topic/cricket
        /// topic/orders


    }

    @Override
    public void registryStopEndpoints(stopEndpointRegisty register){
        register.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }



}

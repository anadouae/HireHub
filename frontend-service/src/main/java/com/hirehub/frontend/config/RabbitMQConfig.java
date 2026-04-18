package com.hirehub.frontend.config;

import com.hirehub.common.constants.RabbitMQConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange hirehubExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
    }
}

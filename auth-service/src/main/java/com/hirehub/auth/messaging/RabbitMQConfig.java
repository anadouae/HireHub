package com.hirehub.auth.messaging;

import com.hirehub.common.notification.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange hirehubExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue authRecruiterVerifiedQueue() {
        return new Queue(RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED, true, false, false);
    }

    @Bean
    public Binding bindingAuthRecruiterVerified(Queue authRecruiterVerifiedQueue, TopicExchange hirehubExchange) {
        return BindingBuilder.bind(authRecruiterVerifiedQueue)
                .to(hirehubExchange)
                .with(RabbitMQConstants.ROUTING_RECRUITER_VERIFIED);
    }
}

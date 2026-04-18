package com.hirehub.verification.config;

import com.hirehub.common.constants.RabbitMQConstants;
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
    public Queue recruiterVerificationQueue() {
        return new Queue(RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER, true, false, false);
    }

    @Bean
    public Binding bindingRecruiterRegistered(Queue recruiterVerificationQueue, TopicExchange hirehubExchange) {
        return BindingBuilder.bind(recruiterVerificationQueue)
                .to(hirehubExchange)
                .with(RabbitMQConstants.ROUTING_RECRUITER_REGISTERED);
    }
}

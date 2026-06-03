package com.hirehub.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.hirehub.common.notification.RabbitMQJsonConfig;

@SpringBootApplication
@Import(RabbitMQJsonConfig.class)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

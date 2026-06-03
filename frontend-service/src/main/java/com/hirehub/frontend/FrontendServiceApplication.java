package com.hirehub.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import com.hirehub.common.notification.RabbitMQJsonConfig;

@SpringBootApplication
@EnableFeignClients
@Import(RabbitMQJsonConfig.class)
public class FrontendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendServiceApplication.class, args);
    }
}

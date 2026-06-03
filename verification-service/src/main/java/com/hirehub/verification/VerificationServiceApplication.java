package com.hirehub.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import com.hirehub.common.notification.RabbitMQJsonConfig;

@SpringBootApplication
@EnableAsync
@Import(RabbitMQJsonConfig.class)
public class VerificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerificationServiceApplication.class, args);
    }
}

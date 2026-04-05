package com.hirehub.candidature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Application principale pour le service de candidatures
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"com.hirehub"})
public class CandidatureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandidatureServiceApplication.class, args);
    }
}

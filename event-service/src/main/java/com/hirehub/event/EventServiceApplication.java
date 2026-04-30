package com.hirehub.event;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║              EventService - Audit & Monitoring                ║
 * ║                                                                ║
 * ║  Rôle: Service léger d'audit et de tracking des événements   ║
 * ║  - Consomme les événements pour audit/logs                   ║
 * ║  - Persiste l'historique des événements                      ║
 * ║  - Expose des métriques / statistiques                       ║
 * ║                                                                ║
 * ║  ⚠️  N'est PAS un point de passage obligatoire               ║
 * ║  Chaque service consomme directement les événements          ║
 * ║  qui le concernent via notification-service                  ║
 * ║                                                                ║
 * ║  Architecture (conforme aux bonnes pratiques entreprise):     ║
 * ║  - RabbitMQ = bus d'événements partagé                       ║
 * ║  - notification-service = consommateur final des mails       ║
 * ║  - event-service = audit optionnel                           ║
 * ║  - Pas de Feign inter-services pour les mails                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@EnableRabbit
@SpringBootApplication(scanBasePackages = {
        "com.hirehub.event",
        "com.hirehub.common"
})
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}

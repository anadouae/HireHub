package com.hirehub.notification.listener;

import com.hirehub.common.notification.EventMessageDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.notification.EmailBusinessServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**<pre>
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  Authentification Listener - Emails OTP, confirmations       ║
 * ║                                                              ║
 * ║  Consomme: user.authentification.register/login/logout       ║
 * ║  Action: Envoyer emails OTP, confirmations, etc.             ║
 * ║                                                              ║
 * ║  Exemple de message (REGISTER):                              ║
 * ║  {                                                           ║
 * ║    "eventType": "REGISTER",                                  ║
 * ║    "email": "newuser@example.com",                           ║
 * ║    "fullName": "Jean Dupont",                                ║
 * ║    "otpCode": "123456",                                      ║
 * ║    "expiresAt": "2026-04-15T10:30:00"                        ║
 * ║  }                                                           ║
 * ║                                                              ║
 * ║  Exemple de message (LOGIN):                                 ║
 * ║  {                                                           ║
 * ║    "eventType": "LOGIN",                                     ║
 * ║    "email": "user@example.com",                              ║
 * ║    "ipAddress": "192.168.1.1",                               ║
 * ║    "timestamp": "2026-04-15T08:45:0"                         ║
 * ║  }                                                           ║
 * ╚══════════════════════════════════════════════════════════════╝</pre>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationListener {

    private final EmailBusinessServiceImpl emailService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION)
    public void handleAuthenticationEvent(@Payload EventMessageDTO message) {
        try {
            Map<String, Object> payload = message.getPayload();
            String eventType = (String) payload.get("eventType");
            String email = (String) payload.get("email");

            switch (eventType) {
                case "REGISTER":
                    handleRegister(payload);
                    break;
                case "LOGIN":
                    handleLogin(payload);
                    break;
                case "LOGOUT":
                    handleLogout(payload);
                    break;
                case "PASSWORD_RESET":
                    handlePasswordReset(payload);
                    break;
                default:
                    log.warn("[AUTH] Événement non reconnu: {}", eventType);
            }

            log.info("[AUTH] Événement {} traité pour: {}", eventType, email);

        } catch (Exception e) {
            log.error("[❌ AUTH] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement authentification", e);
        }
    }

    private void handleRegister(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String fullName = (String) payload.get("fullName");
        String otpCode = (String) payload.get("otpCode");
        String expiresAt = (String) payload.get("expiresAt");

        log.info("[REGISTER] Envoi du mail OTP à: {} | OTP expires: {}", email, expiresAt);

        // TODO: emailService.sendOtpEmail(email, fullName, otpCode, expiresAt);
    }

    private void handleLogin(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String ipAddress = (String) payload.get("ipAddress");
        String timestamp = (String) payload.get("timestamp");

        log.info("[LOGIN] Notification de connexion envoyée à: {} depuis IP: {}", email, ipAddress);

        // TODO: emailService.sendLoginConfirmationEmail(email, ipAddress, timestamp);
    }

    private void handleLogout(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String timestamp = (String) payload.get("timestamp");

        log.info("[LOGOUT] Notification de déconnexion envoyée à: {}", email);

        // TODO: emailService.sendLogoutNotificationEmail(email, timestamp);
    }

    private void handlePasswordReset(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String resetToken = (String) payload.get("resetToken");
        String expiresAt = (String) payload.get("expiresAt");

        log.info("[PASSWORD RESET] Lien de réinitialisation envoyé à: {}", email);

        // TODO: emailService.sendPasswordResetEmail(email, resetToken, expiresAt);
    }
}


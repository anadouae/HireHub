package com.hirehub.notification.services.email.templates;

public class EmailTemplateForAuthentification {
    // ═══════════════════════════════════════════════════════════════
    // TEMPLATES HTML BUILDERS FOR AUTHENTIFICATION-RELATED EMAILS
    // ═══════════════════════════════════════════════════════════════

    public static String buildRegisterOtpTemplate(String userName, String otpCode, int otpValidityMinutes) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { padding: 20px; }
                .otp-box { font-size: 28px; letter-spacing: 6px; font-weight: bold; text-align: center; background: #eff6ff; color: #1d4ed8; padding: 14px; border-radius: 8px; margin: 16px 0; }
                .warning { color: #b91c1c; font-size: 13px; }
                .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>HireHub - Verification OTP</h1>
                </div>
                <div class="content">
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Bienvenue sur HireHub. Utilisez ce code OTP pour finaliser votre inscription :</p>
                    <div class="otp-box">%s</div>
                    <p>Ce code expire dans <strong>%d minutes</strong>.</p>
                    <p class="warning">Ne partagez jamais ce code. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
                    <p>Cordialement,<br>L'equipe HireHub</p>
                </div>
                <div class="footer">
                    <p>© 2026 HireHub. Tous droits reserves.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(userName, otpCode, otpValidityMinutes);
    }

    public static String buildLoginAlertTemplate(String userName, String loginDateTime, String ipAddress, String userAgent) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                .header { background-color: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { padding: 20px; }
                .details { background-color: #ecfdf5; padding: 15px; border-radius: 8px; margin: 15px 0; }
                .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header"><h1>Connexion reussie</h1></div>
                <div class="content">
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Une connexion a ete detectee sur votre compte HireHub.</p>
                    <div class="details">
                        <p><strong>Date/heure:</strong> %s</p>
                        <p><strong>Adresse IP:</strong> %s</p>
                        <p><strong>Appareil/Navigateur:</strong> %s</p>
                    </div>
                    <p>Si ce n'etait pas vous, veuillez changer votre mot de passe immediatement.</p>
                    <p>Cordialement,<br>L'equipe HireHub</p>
                </div>
                <div class="footer"><p>© 2026 HireHub. Tous droits reserves.</p></div>
            </div>
        </body>
        </html>
        """.formatted(userName, loginDateTime, ipAddress, userAgent);
    }

    public static String buildLogoutInfoTemplate(String userName, String logoutDateTime) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                .header { background-color: #475569; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { padding: 20px; }
                .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header"><h1>Deconnexion</h1></div>
                <div class="content">
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Votre session HireHub a ete fermee avec succes.</p>
                    <p><strong>Date/heure:</strong> %s</p>
                    <p>Si vous n'etes pas a l'origine de cette action, contactez le support.</p>
                    <p>Cordialement,<br>L'equipe HireHub</p>
                </div>
                <div class="footer"><p>© 2026 HireHub. Tous droits reserves.</p></div>
            </div>
        </body>
        </html>
        """.formatted(userName, logoutDateTime);
    }

}

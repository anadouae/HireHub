package com.hirehub.notification.template;

public class EmailTemplateForCandidature {

    // ═══════════════════════════════════════════════════════════════
    // TEMPLATES HTML BUILDERS FOR CANDIDATURE-RELATED EMAILS
    // ═══════════════════════════════════════════════════════════════

    public static String buildCandidatureConfirmationTemplate(String candidatName, String offreTitle) {
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
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HireHub</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Merci de votre candidature pour le poste de <strong>%s</strong>.</p>
                        <p>Nous avons bien reçu votre dossier et l'examinerons attentivement.</p>
                        <p>Vous recevrez une mise à jour prochainement.</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidatName, offreTitle);
    }

    public static String buildCandidatureStatutChangedTemplate(String candidatName, String offreTitle,
                                                               String nouveauStatut, String commentaire) {
        String statusColor = switch (nouveauStatut.toUpperCase()) {
            case "ACCEPTÉ" -> "#10b981";
            case "REJETÉ" -> "#ef4444";
            case "EN_ATTENTE" -> "#f59e0b";
            default -> "#3b82f6";
        };

        String statusMessage = switch (nouveauStatut.toUpperCase()) {
            case "ACCEPTÉ" -> "Félicitations ! Votre candidature a été acceptée.";
            case "REJETÉ" -> "Nous vous remercions pour votre intérêt.";
            case "EN_ATTENTE" -> "Votre candidature est en attente.";
            default -> "Votre candidature a été mise à jour.";
        };

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
                    .status-badge { background-color: %s; color: white; padding: 15px; text-align: center; border-radius: 8px; margin: 15px 0; font-weight: bold; font-size: 18px; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HireHub</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <div class="status-badge">%s</div>
                        <p>Le statut de votre candidature pour le poste de <strong>%s</strong> a été mis à jour.</p>
                        %s
                        <p>Merci de votre attention.</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(statusColor, candidatName, statusMessage, offreTitle,
                commentaire != null && !commentaire.isBlank()
                        ? "<p><strong>Commentaire:</strong> " + commentaire + "</p>"
                        : "");
    }

    public static String buildCandidatureEntretienTemplate(String candidatName, String offreTitle,
                                                           String dateEntretien, String lieux, String interviewer) {
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
                    .details { background-color: #e5e7eb; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .detail-item { margin: 10px 0; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HireHub</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Nous sommes heureux de vous inviter à un entretien pour le poste de <strong>%s</strong>.</p>
                        <div class="details">
                            <div class="detail-item"><strong>📅 Date et heure:</strong> %s</div>
                            <div class="detail-item"><strong>📍 Lieu:</strong> %s</div>
                            <div class="detail-item"><strong>👤 Interviewer:</strong> %s</div>
                        </div>
                        <p>Veuillez confirmer votre présence à l'adresse ci-dessus.</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidatName, offreTitle, dateEntretien, lieux, interviewer);
    }

    public static String buildCandidatureEntretienAnnulationTemplate(String candidatName, String offreTitle, String raison) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                    .header { background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Annulation d'entretien</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Nous devons malheureusement annuler l'entretien prévu pour le poste de <strong>%s</strong>.</p>
                        %s
                        <p>Nous nous excusons pour le désagrément et vous recontacterons si de nouvelles opportunités se présentent.</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidatName, offreTitle,
                raison != null && !raison.isBlank()
                        ? "<p><strong>Raison:</strong> " + raison + "</p>"
                        : "");
    }

}

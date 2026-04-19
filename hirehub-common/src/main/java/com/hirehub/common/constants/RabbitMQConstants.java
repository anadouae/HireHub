
///╔══════════════════════════════════════════════════════════════╗
///║  Ce fichier EST le contrat RabbitMQ de tout le projet.       ║
///║  Tout service qui publie OU consomme un message              ║
///║  DOIT utiliser ces constantes — jamais de String en dur.     ║
///╚══════════════════════════════════════════════════════════════╝

package com.hirehub.common.constants;

///"final" = cette classe ne peut pas être étendue (héritée)
///Ce n'est pas une entité métier — c'est un répertoire de constantes
public final class RabbitMQConstants {

    ///Constructeur privé = impossible de faire "new RabbitMQConstants()"
    ///On n'a besoin d'aucune instance — on accède tout via RabbitMQConstants.EXCHANGE
    ///C'est le pattern "utility class" standard en Java
    private RabbitMQConstants() {}


    ///─── L'EXCHANGE ────────────────────────────────────────────────
    //
    ///L'exchange est l'aiguilleur central de RabbitMQ.
    ///Tous les services publient VERS cet exchange (pas vers une queue directement).
    ///C'est l'exchange qui décide quelle(s) queue(s) reçoit le message,
    ///selon la routing key et les "bindings" configurés au démarrage.
    //
    ///"hirehub.events" est un nom arbitraire — il doit juste être
    ///identique dans TOUS les services (publisher et consumers).
    ///Un seul exchange pour tout le projet = architecture topic exchange.
    public static final String EXCHANGE = "hirehub.events";


    ///─── LES ROUTING KEYS ──────────────────────────────────────────
    //
    ///La routing key est l'étiquette qu'on colle sur le message au moment
    ///de le publier. C'est elle qui dit "ce message parle de candidature créée".
    //
    ///Convention de nommage : "domaine.evenement" (tout en minuscules, points)
    ///Le publisher (candidature-service) utilise cette constante ici :
    ///  rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_CANDIDATURE_CREATED, event)
    //
    ///Le consumer (notification-service) l'utilise pour configurer son binding :
    ///  @RabbitListener(bindings = @QueueBinding(
    ///      value = @Queue(QUEUE_NOTIFICATION_CANDIDATURE),
    ///      exchange = @Exchange(EXCHANGE),
    ///      key = ROUTING_CANDIDATURE_CREATED))

    ///Publié par : candidature-service, quand un candidat soumet sa candidature
    ///Consommé par : notification-service → envoie email "Candidature reçue"
    public static final String ROUTING_CANDIDATURE_CREATED  = "candidature.created";

    ///Publié par : candidature-service, quand le recruteur change le statut
    ///Consommé par : notification-service → envoie email "Statut mis à jour"
    public static final String ROUTING_STATUT_CHANGED       = "candidature.statut.changed";

    ///Publié par : entretien-service, quand un entretien est planifié ou annulé
    ///Consommé par : notification-service → envoie email date/heure/lieu
    public static final String ROUTING_ENTRETIEN_PLANIFIE   = "entretien.planifie";

    ///(Legacy / optionnel) Publié si le produit notifie un passage en "recruteur actif" — pas une etape admin "approuver inscription".
    ///Consommé par : notification-service → email eventuel
    public static final String ROUTING_RECRUITER_APPROVED   = "recruiter.request.approved";

    ///(Legacy / optionnel) Publié si le produit notifie un refus / restriction — pas une file admin d'inscription.
    ///Consommé par : notification-service → email eventuel
    public static final String ROUTING_RECRUITER_REJECTED   = "recruiter.request.rejected";

    ///Publié par : auth-service, quand un recruteur finit l'inscription
    ///Consommé par : verification-service -> OCR/API + decision async
    public static final String ROUTING_RECRUITER_REGISTERED  = "recruiter.registered";

    ///Publié par : verification-service, résultat final du contrôle documentaire
    ///Consommé par : auth-service -> mise à jour du statut du compte
    public static final String ROUTING_RECRUITER_VERIFIED    = "recruiter.verified";

    ///Publié par : frontend-service (espace admin), lors d'un blocage utilisateur
    ///Consommé par : notification-service pour audit / notification
    public static final String ROUTING_USER_BLOCKED          = "user.blocked";

    ///Publié par : frontend-service (espace admin), lors d'une suppression utilisateur
    ///Consommé par : notification-service pour audit / notification
    public static final String ROUTING_USER_DELETED          = "user.deleted";


    ///─── LES QUEUES ────────────────────────────────────────────────
    //
    ///Une queue est la "boîte aux lettres" d'un consumer.
    ///Chaque queue est bindée à l'exchange + une routing key spécifique.
    ///Quand notification-service démarre, Spring crée ces queues dans RabbitMQ
    ///si elles n'existent pas encore — via la config @Bean dans RabbitConfig.java.
    //
    ///Règle : UNE queue par type d'event consommé par notification-service.
    ///Si demain un 2e service veut aussi consommer "candidature.created",
    ///il aura SA propre queue — chaque consumer a sa boîte aux lettres indépendante.
    ///Ainsi, les deux consumers reçoivent chacun leur copie du message.

    ///Queue écoutée pour les nouvelles candidatures
    ///binding : EXCHANGE + ROUTING_CANDIDATURE_CREATED → cette queue
    public static final String QUEUE_NOTIFICATION_CANDIDATURE = "notif.candidature.queue";

    ///Queue écoutée pour les changements de statut
    ///binding : EXCHANGE + ROUTING_STATUT_CHANGED → cette queue
    public static final String QUEUE_NOTIFICATION_STATUT      = "notif.statut.queue";

    ///Queue écoutée pour les entretiens planifiés/annulés
    ///binding : EXCHANGE + ROUTING_ENTRETIEN_PLANIFIE → cette queue
    public static final String QUEUE_NOTIFICATION_ENTRETIEN   = "notif.entretien.queue";

    ///Queue écoutée pour les décisions admin sur les demandes recruteur
    ///binding : EXCHANGE + ROUTING_RECRUITER_APPROVED/REJECTED → cette queue
    ///(les deux routing keys vont dans la même queue — un seul listener gère les deux)
    public static final String QUEUE_NOTIFICATION_RECRUITER   = "notif.recruiter.queue";

    ///Queue écoutée par verification-service pour lancer le contrôle OCR/API
    public static final String QUEUE_VERIFICATION_RECRUITER   = "verification.recruiter.queue";

    ///Queue écoutée par auth-service pour recevoir le résultat du verification-service
    public static final String QUEUE_AUTH_RECRUITER_VERIFIED  = "auth.recruiter.verified.queue";

    ///Queue notifications/audit pour actions admin sur comptes utilisateurs
    public static final String QUEUE_NOTIFICATION_ADMIN_USER   = "notif.admin.user.queue";
}
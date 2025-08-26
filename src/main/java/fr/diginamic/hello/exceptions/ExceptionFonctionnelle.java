package fr.diginamic.hello.exceptions;

/**
 * Exception métier personnalisée pour les erreurs fonctionnelles
 * 
 * Cette exception est utilisée pour gérer les erreurs liées à la logique métier
 * de l'application (validation, règles de gestion, contraintes fonctionnelles)
 * 
 * @author Votre nom
 * @version 1.0
 */
public class ExceptionFonctionnelle extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String code;
    // messageKey permet l'i18n explicite, distinct du code (classification)
    private final String messageKey;
    private final Object[] args;

    /**
     * Constructeur avec message simple
     * @param message le message d'erreur
     */
    public ExceptionFonctionnelle(String message) {
        super(message);
        this.code = null;
        this.messageKey = null;
        this.args = null;
    }

    /**
     * Constructeur avec message et cause
     * @param message le message d'erreur
     * @param cause la cause de l'exception
     */
    public ExceptionFonctionnelle(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
        this.messageKey = null;
        this.args = null;
    }

    /**
     * Constructeur avec code d'erreur et message
     * @param code le code d'erreur
     * @param message le message d'erreur
     */
    public ExceptionFonctionnelle(String code, String message) {
        super(message);
        this.code = code;
        this.messageKey = null;
        this.args = null;
    }

    /**
     * Constructeur avec code d'erreur, message et arguments
     * @param code le code d'erreur
     * @param message le message d'erreur
     * @param args les arguments pour le formatage du message
     */
    public ExceptionFonctionnelle(String code, String message, Object... args) {
        super(String.format(message, args));
        this.code = code;
        this.messageKey = null;
        this.args = args;
    }

    /**
     * Constructeur i18n: code de classification + clé de message + args
     */
    public ExceptionFonctionnelle(String code, String messageKey, Object[] args, String fallbackMessage) {
        super(fallbackMessage);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }

    // ==================== MÉTHODES STATIQUES POUR LES ERREURS COURANTES ====================

    public static ExceptionFonctionnelle ressourceNonTrouvee(String type, Object id) {
        return new ExceptionFonctionnelle(
            "RESOURCE_NOT_FOUND",
            "error.business.RESOURCE_NOT_FOUND",
            new Object[]{type, id},
            String.format("%s non trouvé(e) avec l'identifiant : %s", type, id)
        );
    }

    public static ExceptionFonctionnelle ressourceDejaExistante(String type, String champ, Object valeur) {
        return new ExceptionFonctionnelle(
            "RESOURCE_ALREADY_EXISTS",
            "error.business.RESOURCE_ALREADY_EXISTS",
            new Object[]{type, champ, valeur},
            String.format("%s avec %s '%s' existe déjà", type, champ, valeur)
        );
    }

    public static ExceptionFonctionnelle suppressionImpossible(String type, String raison) {
        return new ExceptionFonctionnelle(
            "DELETE_FORBIDDEN",
            "error.business.DELETE_FORBIDDEN",
            new Object[]{type, raison},
            String.format("Impossible de supprimer %s : %s", type, raison)
        );
    }

    public static ExceptionFonctionnelle donneesInvalides(String message) {
        return new ExceptionFonctionnelle("INVALID_DATA", message);
    }

    public static ExceptionFonctionnelle contrainteViolee(String contrainte, Object valeur) {
        return new ExceptionFonctionnelle(
            "CONSTRAINT_VIOLATION",
            "error.business.CONSTRAINT_VIOLATION",
            new Object[]{contrainte, valeur},
            String.format("Contrainte '%s' violée pour la valeur : %s", contrainte, valeur)
        );
    }

    public static ExceptionFonctionnelle operationNonAutorisee(String operation, String raison) {
        return new ExceptionFonctionnelle(
            "OPERATION_FORBIDDEN",
            "error.business.OPERATION_FORBIDDEN",
            new Object[]{operation, raison},
            String.format("Opération '%s' non autorisée : %s", operation, raison)
        );
    }
}

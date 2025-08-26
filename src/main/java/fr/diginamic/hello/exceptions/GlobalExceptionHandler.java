package fr.diginamic.hello.exceptions;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'application
 * 
 * Cette classe intercepte toutes les exceptions lancées par les contrôleurs
 * et les transforme en réponses HTTP appropriées avec des messages personnalisés.
 * 
 * @author Votre nom
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    /**
     * Structure de réponse d'erreur standardisée
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String code;
        private String path;
        private Map<String, String> details;

        public ErrorResponse() {
            this.timestamp = LocalDateTime.now();
            this.details = new HashMap<>();
        }

        public ErrorResponse(int status, String error, String message, String path) {
            this();
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        // Getters et Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public Map<String, String> getDetails() { return details; }
        public void setDetails(Map<String, String> details) { this.details = details; }
    }

    /**
     * Gère les exceptions fonctionnelles métier
     */
    @ExceptionHandler(ExceptionFonctionnelle.class)
    public ResponseEntity<ErrorResponse> handleExceptionFonctionnelle(
            ExceptionFonctionnelle ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        String title = messageSource.getMessage("error.business.title", null, locale);
        String messageKey = ex.getMessageKey() != null
                ? ex.getMessageKey()
                : (ex.getCode() != null ? "error.business." + ex.getCode() : null);
        String resolvedMessage = messageKey != null
                ? messageSource.getMessage(messageKey, ex.getArgs(), ex.getMessage(), locale)
                : ex.getMessage();

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            title,
            resolvedMessage,
            request.getDescription(false).replace("uri=", "")
        );
        
        if (ex.getCode() != null) {
            error.setCode(ex.getCode());
        }

        // Déterminer le status HTTP selon le type d'erreur
        HttpStatus status = determineHttpStatus(ex.getCode());
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());

        return new ResponseEntity<>(error, status);
    }

    /**
     * Gère les exceptions de validation des formulaires
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            messageSource.getMessage("error.validation.title", null, locale),
            messageSource.getMessage("error.validation.message", null, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("VALIDATION_ERROR");
        
        // Ajouter les détails des erreurs de validation
        BindingResult result = ex.getBindingResult();
        for (FieldError fieldError : result.getFieldErrors()) {
            error.getDetails().put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les exceptions de validation des contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            messageSource.getMessage("error.constraint.title", null, locale),
            messageSource.getMessage("error.constraint.message", null, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("CONSTRAINT_VIOLATION");

        // Ajouter les détails des contraintes violées
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            error.getDetails().put(propertyPath, message);
        });

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les exceptions d'entité non trouvée (JPA)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            messageSource.getMessage("error.entityNotFound.title", null, locale),
            ex.getMessage() != null ? ex.getMessage() : messageSource.getMessage("error.entityNotFound.message.default", null, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("ENTITY_NOT_FOUND");

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Gère les exceptions de binding (formulaires)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            messageSource.getMessage("error.bind.title", null, locale),
            messageSource.getMessage("error.bind.message", null, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("BIND_ERROR");

        // Ajouter les erreurs de binding
        for (FieldError fieldError : ex.getFieldErrors()) {
            error.getDetails().put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les violations d'intégrité des données (contraintes SQL)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            messageSource.getMessage("error.dataIntegrity.title", null, locale),
            messageSource.getMessage("error.dataIntegrity.message.default", null, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("CONSTRAINT_VIOLATION");
        
        // Analyser le message d'erreur pour donner des infos plus précises
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("format_code_departement")) {
                error.setMessage(messageSource.getMessage("error.dataIntegrity.message.formatCodeDept", null, locale));
                error.getDetails().put("constraint", "format_code_departement");
                error.getDetails().put("help", messageSource.getMessage("error.dataIntegrity.help.formatCodeDept", null, locale));
            } else if (message.contains("duplicate key") || message.contains("unique")) {
                error.setMessage(messageSource.getMessage("error.dataIntegrity.message.duplicate", null, locale));
                error.getDetails().put("constraint", "unique_constraint");
            } else {
                error.setMessage(messageSource.getMessage("error.dataIntegrity.message.generic", null, locale));
                error.getDetails().put("sqlError", message);
            }
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs de type de paramètre de méthode
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            messageSource.getMessage("error.methodArgumentType.title", null, locale),
            messageSource.getMessage("error.methodArgumentType.message", 
                new Object[]{ex.getName(), ex.getValue()}, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("METHOD_ARGUMENT_TYPE_MISMATCH");
        error.getDetails().put("parameter", ex.getName());
        error.getDetails().put("value", String.valueOf(ex.getValue()));
        error.getDetails().put("requiredType", ex.getRequiredType().getSimpleName());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère toutes les autres exceptions non prévues
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, WebRequest request) {
        
        Locale locale = LocaleContextHolder.getLocale();
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            messageSource.getMessage("error.general.title", null, locale),
            messageSource.getMessage("error.general.message", null, locale),
            request.getDescription(false).replace("uri=", "")
        );
        
        error.setCode("INTERNAL_ERROR");
        
        // En développement, on peut ajouter plus de détails
        error.getDetails().put("exceptionType", ex.getClass().getSimpleName());
        error.getDetails().put("cause", ex.getMessage() != null ? ex.getMessage() : "Aucune cause spécifiée");

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Détermine le status HTTP approprié selon le code d'erreur
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        
        return switch (errorCode) {
            case "RESOURCE_NOT_FOUND", "ENTITY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "RESOURCE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "DELETE_FORBIDDEN", "OPERATION_FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "INVALID_DATA", "VALIDATION_ERROR", "CONSTRAINT_VIOLATION" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}

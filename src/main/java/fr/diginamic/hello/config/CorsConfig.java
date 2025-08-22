package fr.diginamic.hello.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuration CORS pour permettre l'accès depuis le frontend
 * 
 * Cette configuration permet au frontend (servé par Spring Boot sur le port 8081)
 * d'accéder aux endpoints REST API depuis différentes origines
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configuration CORS globale pour tous les endpoints
     * Permet l'accès depuis localhost avec différents ports et protocoles
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Permet tous les origins en développement
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight pendant 1 heure
    }

    /**
     * Configuration CORS plus spécifique via bean
     * Alternative à la méthode addCorsMappings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origins autorisées (localhost avec différents ports)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "file://*" // Pour les fichiers HTML ouverts directement
        ));
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization", 
            "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        
        // Headers exposés au frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
package fr.diginamic.hello.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration de la documentation OpenAPI/Swagger pour l'API Hello.
 * Cette classe configure les métadonnées de l'API et les serveurs disponibles.
 *
 * @author Matthieu
 * @version 1.0
 * @since 2025-08-26
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * Configuration personnalisée de l'OpenAPI.
     * Définit les informations générales de l'API, les contacts et les serveurs.
     *
     * @return OpenAPI configuré avec les métadonnées de l'API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Hello - Gestion des Villes et Départements")
                        .version("1.0.0")
                        .description("""
                                Cette API REST fournit des services pour la gestion des villes et départements français.
                                
                                **Fonctionnalités principales :**
                                - Consultation des villes et départements
                                - Recherche par différents critères
                                - Gestion CRUD complète
                                - Validation des données d'entrée
                                - Gestion des erreurs avec codes de statut appropriés
                                
                                **Technologies utilisées :**
                                - Spring Boot 3.5.4
                                - Spring Data JPA
                                - MySQL
                                - Bean Validation
                                """)
                        .contact(new Contact()
                                .name("Matthieu - Développeur Full Stack")
                                .email("matthieu@diginamic.fr")
                                .url("https://github.com/matthieu"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Serveur de développement local"),
                        new Server()
                                .url("https://api-hello.mondomaine.fr" + contextPath)
                                .description("Serveur de production")
                ));
    }
}
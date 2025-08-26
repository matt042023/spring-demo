package fr.diginamic.hello.services;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String salutation() {
        return "🏛️ API de Gestion des Villes et Départements Français - Cette application permet de gérer les données territoriales avec des fonctionnalités CRUD complètes, pagination, recherches avancées par nom/population/département, statistiques en temps réel et export JSON. Interface moderne avec outils de recherche multi-critères. Développée avec Spring Boot + JPA pour le backend et JavaScript vanilla pour le frontend. 🔍 Les recherches avancées retournent des données JSON détaillées pour une intégration facile avec d'autres systèmes.";
    }
}

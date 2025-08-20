package fr.diginamic.hello.controlers;

import fr.diginamic.hello.models.Ville;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/villes")
public class VilleController {

    private List<Ville> villes;


    public VilleController() {

        this.villes = new ArrayList<>();

        // Ajout de quelques villes d'exemple
        villes.add(new Ville("Paris", 2161000));
        villes.add(new Ville("Marseille", 861635));
        villes.add(new Ville("Lyon", 513275));
        villes.add(new Ville("Toulouse", 471941));
        villes.add(new Ville("Nice", 342637));
        villes.add(new Ville("Nantes", 309346));
        villes.add(new Ville("Montpellier", 285121));
        villes.add(new Ville("Strasbourg", 277270));
    }

    @GetMapping
    public List<Ville> getVilles() {
        return villes;
    }

    @PostMapping
    public ResponseEntity<String> ajouerVille(@RequestBody Ville nouvelleVille) {
        for(Ville ville : villes) {
            if (ville.getNom().equalsIgnoreCase(nouvelleVille.getNom())) {
                return ResponseEntity.badRequest().body("La ville existe déjà");
            }
        }

            villes.add(nouvelleVille);

            return ResponseEntity.ok("Ville inséréé avec succès");
        }

}

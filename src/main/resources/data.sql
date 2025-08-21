-- Script de peuplement de la base de données
-- 20 villes françaises réelles avec leurs départements associés

-- Insertion des départements
INSERT INTO departement (code, nom) VALUES 
('75', 'Paris'),
('13', 'Bouches-du-Rhône'),
('69', 'Rhône'),
('31', 'Haute-Garonne'),
('59', 'Nord'),
('44', 'Loire-Atlantique'),
('33', 'Gironde'),
('34', 'Hérault'),
('06', 'Alpes-Maritimes'),
('67', 'Bas-Rhin'),
('35', 'Ille-et-Vilaine'),
('38', 'Isère'),
('21', 'Côte-d\'Or'),
('30', 'Gard'),
('84', 'Vaucluse');

-- Insertion des villes avec leurs départements
INSERT INTO ville (nom, nb_habitants, departement_id) VALUES
-- Paris (75)
('Paris', 2165423, (SELECT id FROM departement WHERE code = '75')),

-- Bouches-du-Rhône (13)
('Marseille', 870018, (SELECT id FROM departement WHERE code = '13')),
('Aix-en-Provence', 145071, (SELECT id FROM departement WHERE code = '13')),

-- Rhône (69)
('Lyon', 522228, (SELECT id FROM departement WHERE code = '69')),
('Villeurbanne', 149019, (SELECT id FROM departement WHERE code = '69')),

-- Haute-Garonne (31)
('Toulouse', 486828, (SELECT id FROM departement WHERE code = '31')),
('Colomiers', 39192, (SELECT id FROM departement WHERE code = '31')),

-- Nord (59)
('Lille', 232787, (SELECT id FROM departement WHERE code = '59')),
('Roubaix', 95721, (SELECT id FROM departement WHERE code = '59')),

-- Loire-Atlantique (44)
('Nantes', 320732, (SELECT id FROM departement WHERE code = '44')),
('Saint-Nazaire', 69993, (SELECT id FROM departement WHERE code = '44')),

-- Gironde (33)
('Bordeaux', 257804, (SELECT id FROM departement WHERE code = '33')),

-- Hérault (34)
('Montpellier', 295542, (SELECT id FROM departement WHERE code = '34')),

-- Alpes-Maritimes (06)
('Nice', 340017, (SELECT id FROM departement WHERE code = '06')),

-- Bas-Rhin (67)
('Strasbourg', 284677, (SELECT id FROM departement WHERE code = '67')),

-- Ille-et-Vilaine (35)
('Rennes', 217728, (SELECT id FROM departement WHERE code = '35')),

-- Isère (38)
('Grenoble', 158552, (SELECT id FROM departement WHERE code = '38')),

-- Côte-d'Or (21)
('Dijon', 156920, (SELECT id FROM departement WHERE code = '21')),

-- Gard (30)
('Nîmes', 148561, (SELECT id FROM departement WHERE code = '30')),

-- Vaucluse (84)
('Avignon', 91921, (SELECT id FROM departement WHERE code = '84'));
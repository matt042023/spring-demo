-- Script de peuplement de la base de données avec les départements et villes françaises
-- Données factuelles et géographiquement correctes

-- ========================================
-- NETTOYAGE INITIAL
-- ========================================

DELETE FROM ville;
DELETE FROM departement;

-- ========================================
-- DÉPARTEMENTS FRANÇAIS
-- ========================================

INSERT INTO departement (code, nom) VALUES 
('01', 'Ain'),
('02', 'Aisne'),
('03', 'Allier'),
('04', 'Alpes-de-Haute-Provence'),
('05', 'Hautes-Alpes'),
('06', 'Alpes-Maritimes'),
('07', 'Ardèche'),
('08', 'Ardennes'),
('09', 'Ariège'),
('10', 'Aube'),
('11', 'Aude'),
('12', 'Aveyron'),
('13', 'Bouches-du-Rhône'),
('14', 'Calvados'),
('15', 'Cantal'),
('16', 'Charente'),
('17', 'Charente-Maritime'),
('18', 'Cher'),
('19', 'Corrèze'),
('21', 'Côte-d\'Or'),
('22', 'Côtes-d\'Armor'),
('23', 'Creuse'),
('24', 'Dordogne'),
('25', 'Doubs'),
('26', 'Drôme'),
('27', 'Eure'),
('28', 'Eure-et-Loir'),
('29', 'Finistère'),
('30', 'Gard'),
('31', 'Haute-Garonne'),
('32', 'Gers'),
('33', 'Gironde'),
('34', 'Hérault'),
('35', 'Ille-et-Vilaine'),
('36', 'Indre'),
('37', 'Indre-et-Loire'),
('38', 'Isère'),
('39', 'Jura'),
('40', 'Landes'),
('41', 'Loir-et-Cher'),
('42', 'Loire'),
('43', 'Haute-Loire'),
('44', 'Loire-Atlantique'),
('45', 'Loiret'),
('46', 'Lot'),
('47', 'Lot-et-Garonne'),
('48', 'Lozère'),
('49', 'Maine-et-Loire'),
('50', 'Manche'),
('51', 'Marne'),
('52', 'Haute-Marne'),
('53', 'Mayenne'),
('54', 'Meurthe-et-Moselle'),
('55', 'Meuse'),
('56', 'Morbihan'),
('57', 'Moselle'),
('58', 'Nièvre'),
('59', 'Nord'),
('60', 'Oise'),
('61', 'Orne'),
('62', 'Pas-de-Calais'),
('63', 'Puy-de-Dôme'),
('64', 'Pyrénées-Atlantiques'),
('65', 'Hautes-Pyrénées'),
('66', 'Pyrénées-Orientales'),
('67', 'Bas-Rhin'),
('68', 'Haut-Rhin'),
('69', 'Rhône'),
('70', 'Haute-Saône'),
('71', 'Saône-et-Loire'),
('72', 'Sarthe'),
('73', 'Savoie'),
('74', 'Haute-Savoie'),
('75', 'Paris'),
('76', 'Seine-Maritime'),
('77', 'Seine-et-Marne'),
('78', 'Yvelines'),
('79', 'Deux-Sèvres'),
('80', 'Somme'),
('81', 'Tarn'),
('82', 'Tarn-et-Garonne'),
('83', 'Var'),
('84', 'Vaucluse'),
('85', 'Vendée'),
('86', 'Vienne'),
('87', 'Haute-Vienne'),
('88', 'Vosges'),
('89', 'Yonne'),
('90', 'Territoire de Belfort'),
('91', 'Essonne'),
('92', 'Hauts-de-Seine'),
('93', 'Seine-Saint-Denis'),
('94', 'Val-de-Marne'),
('95', 'Val-d\'Oise');

-- ========================================
-- VILLES FRANÇAISES AVEC CORRESPONDANCES EXACTES
-- ========================================

-- Utilisation des sous-requêtes pour les correspondances exactes département-ville
INSERT INTO ville (nom, nb_habitants, departement_id) VALUES 

-- Paris (75) - Département Paris
('Paris', 2165423, (SELECT id FROM departement WHERE code = '75')),

-- Marseille (13) - Département Bouches-du-Rhône
('Marseille', 873076, (SELECT id FROM departement WHERE code = '13')),
('Aix-en-Provence', 145071, (SELECT id FROM departement WHERE code = '13')),
('Arles', 51614, (SELECT id FROM departement WHERE code = '13')),
('Martigues', 48821, (SELECT id FROM departement WHERE code = '13')),

-- Lyon (69) - Département Rhône
('Lyon', 518635, (SELECT id FROM departement WHERE code = '69')),
('Villeurbanne', 149019, (SELECT id FROM departement WHERE code = '69')),
('Vénissieux', 65638, (SELECT id FROM departement WHERE code = '69')),
('Saint-Priest', 47177, (SELECT id FROM departement WHERE code = '69')),

-- Toulouse (31) - Département Haute-Garonne
('Toulouse', 479553, (SELECT id FROM departement WHERE code = '31')),
('Colomiers', 39192, (SELECT id FROM departement WHERE code = '31')),
('Tournefeuille', 29547, (SELECT id FROM departement WHERE code = '31')),
('Blagnac', 24256, (SELECT id FROM departement WHERE code = '31')),

-- Nice (06) - Département Alpes-Maritimes
('Nice', 342637, (SELECT id FROM departement WHERE code = '06')),
('Cannes', 74152, (SELECT id FROM departement WHERE code = '06')),
('Antibes', 75820, (SELECT id FROM departement WHERE code = '06')),
('Grasse', 51107, (SELECT id FROM departement WHERE code = '06')),

-- Nantes (44) - Département Loire-Atlantique
('Nantes', 320732, (SELECT id FROM departement WHERE code = '44')),
('Saint-Nazaire', 69993, (SELECT id FROM departement WHERE code = '44')),
('Saint-Herblain', 46357, (SELECT id FROM departement WHERE code = '44')),
('Rezé', 42424, (SELECT id FROM departement WHERE code = '44')),

-- Montpellier (34) - Département Hérault
('Montpellier', 295542, (SELECT id FROM departement WHERE code = '34')),
('Béziers', 77177, (SELECT id FROM departement WHERE code = '34')),
('Sète', 44270, (SELECT id FROM departement WHERE code = '34')),
('Lunel', 26055, (SELECT id FROM departement WHERE code = '34')),

-- Strasbourg (67) - Département Bas-Rhin
('Strasbourg', 284677, (SELECT id FROM departement WHERE code = '67')),
('Schiltigheim', 32606, (SELECT id FROM departement WHERE code = '67')),
('Haguenau', 35107, (SELECT id FROM departement WHERE code = '67')),
('Illkirch-Graffenstaden', 27077, (SELECT id FROM departement WHERE code = '67')),

-- Bordeaux (33) - Département Gironde
('Bordeaux', 254436, (SELECT id FROM departement WHERE code = '33')),
('Mérignac', 69413, (SELECT id FROM departement WHERE code = '33')),
('Pessac', 62793, (SELECT id FROM departement WHERE code = '33')),
('Talence', 42637, (SELECT id FROM departement WHERE code = '33')),

-- Lille (59) - Département Nord
('Lille', 236234, (SELECT id FROM departement WHERE code = '59')),
('Tourcoing', 97476, (SELECT id FROM departement WHERE code = '59')),
('Roubaix', 95721, (SELECT id FROM departement WHERE code = '59')),
('Dunkerque', 87353, (SELECT id FROM departement WHERE code = '59')),
('Villeneuve-d\'Ascq', 61151, (SELECT id FROM departement WHERE code = '59')),

-- Rennes (35) - Département Ille-et-Vilaine
('Rennes', 217728, (SELECT id FROM departement WHERE code = '35')),
('Saint-Malo', 46478, (SELECT id FROM departement WHERE code = '35')),
('Fougères', 20418, (SELECT id FROM departement WHERE code = '35')),
('Vitré', 18605, (SELECT id FROM departement WHERE code = '35')),

-- Reims (51) - Département Marne
('Reims', 182460, (SELECT id FROM departement WHERE code = '51')),
('Châlons-en-Champagne', 44896, (SELECT id FROM departement WHERE code = '51')),
('Épernay', 23307, (SELECT id FROM departement WHERE code = '51')),

-- Toulon (83) - Département Var
('Toulon', 176198, (SELECT id FROM departement WHERE code = '83')),
('Hyères', 55588, (SELECT id FROM departement WHERE code = '83')),
('Fréjus', 54458, (SELECT id FROM departement WHERE code = '83')),
('La Seyne-sur-Mer', 65319, (SELECT id FROM departement WHERE code = '83')),
('Draguignan', 39315, (SELECT id FROM departement WHERE code = '83')),

-- Saint-Étienne (42) - Département Loire
('Saint-Étienne', 171057, (SELECT id FROM departement WHERE code = '42')),
('Roanne', 35761, (SELECT id FROM departement WHERE code = '42')),
('Saint-Chamond', 35707, (SELECT id FROM departement WHERE code = '42')),
('Montbrison', 15678, (SELECT id FROM departement WHERE code = '42')),

-- Le Havre (76) - Département Seine-Maritime
('Le Havre', 170147, (SELECT id FROM departement WHERE code = '76')),
('Rouen', 110145, (SELECT id FROM departement WHERE code = '76')),
('Sotteville-lès-Rouen', 29260, (SELECT id FROM departement WHERE code = '76')),
('Saint-Étienne-du-Rouvray', 28209, (SELECT id FROM departement WHERE code = '76')),

-- Grenoble (38) - Département Isère
('Grenoble', 158454, (SELECT id FROM departement WHERE code = '38')),
('Saint-Martin-d\'Hères', 38067, (SELECT id FROM departement WHERE code = '38')),
('Échirolles', 35842, (SELECT id FROM departement WHERE code = '38')),
('Fontaine', 22063, (SELECT id FROM departement WHERE code = '38')),

-- Dijon (21) - Département Côte-d'Or
('Dijon', 156920, (SELECT id FROM departement WHERE code = '21')),
('Beaune', 21923, (SELECT id FROM departement WHERE code = '21')),
('Chenôve', 13672, (SELECT id FROM departement WHERE code = '21')),

-- Angers (49) - Département Maine-et-Loire
('Angers', 155850, (SELECT id FROM departement WHERE code = '49')),
('Cholet', 54204, (SELECT id FROM departement WHERE code = '49')),
('Saumur', 26973, (SELECT id FROM departement WHERE code = '49')),

-- Nîmes (30) - Département Gard
('Nîmes', 148561, (SELECT id FROM departement WHERE code = '30')),
('Alès', 40400, (SELECT id FROM departement WHERE code = '30')),
('Bagnols-sur-Cèze', 18044, (SELECT id FROM departement WHERE code = '30')),

-- Le Mans (72) - Département Sarthe
('Le Mans', 143813, (SELECT id FROM departement WHERE code = '72')),
('Sablé-sur-Sarthe', 12051, (SELECT id FROM departement WHERE code = '72')),
('La Flèche', 14842, (SELECT id FROM departement WHERE code = '72')),

-- Brest (29) - Département Finistère
('Brest', 139456, (SELECT id FROM departement WHERE code = '29')),
('Quimper', 63360, (SELECT id FROM departement WHERE code = '29')),
('Concarneau', 19554, (SELECT id FROM departement WHERE code = '29')),
('Morlaix', 14810, (SELECT id FROM departement WHERE code = '29')),

-- Clermont-Ferrand (63) - Département Puy-de-Dôme
('Clermont-Ferrand', 147284, (SELECT id FROM departement WHERE code = '63')),
('Chamalières', 17370, (SELECT id FROM departement WHERE code = '63')),
('Riom', 18469, (SELECT id FROM departement WHERE code = '63')),

-- Amiens (80) - Département Somme
('Amiens', 133755, (SELECT id FROM departement WHERE code = '80')),
('Abbeville', 23067, (SELECT id FROM departement WHERE code = '80')),
('Montdidier', 6084, (SELECT id FROM departement WHERE code = '80')),

-- Limoges (87) - Département Haute-Vienne
('Limoges', 132175, (SELECT id FROM departement WHERE code = '87')),
('Saint-Junien', 10713, (SELECT id FROM departement WHERE code = '87')),
('Panazol', 10558, (SELECT id FROM departement WHERE code = '87')),

-- Tours (37) - Département Indre-et-Loire
('Tours', 136463, (SELECT id FROM departement WHERE code = '37')),
('Joué-lès-Tours', 38294, (SELECT id FROM departement WHERE code = '37')),
('Saint-Pierre-des-Corps', 15470, (SELECT id FROM departement WHERE code = '37')),

-- Perpignan (66) - Département Pyrénées-Orientales
('Perpignan', 121875, (SELECT id FROM departement WHERE code = '66')),
('Canet-en-Roussillon', 12478, (SELECT id FROM departement WHERE code = '66')),
('Saint-Estève', 11753, (SELECT id FROM departement WHERE code = '66')),

-- Metz (57) - Département Moselle
('Metz', 117492, (SELECT id FROM departement WHERE code = '57')),
('Thionville', 40701, (SELECT id FROM departement WHERE code = '57')),
('Montigny-lès-Metz', 22595, (SELECT id FROM departement WHERE code = '57')),
('Sarreguemines', 20743, (SELECT id FROM departement WHERE code = '57')),

-- Besançon (25) - Département Doubs
('Besançon', 116914, (SELECT id FROM departement WHERE code = '25')),
('Montbéliard', 25395, (SELECT id FROM departement WHERE code = '25')),
('Pontarlier', 17356, (SELECT id FROM departement WHERE code = '25')),

-- Boulogne-Billancourt (92) - Département Hauts-de-Seine
('Boulogne-Billancourt', 120071, (SELECT id FROM departement WHERE code = '92')),
('Nanterre', 96807, (SELECT id FROM departement WHERE code = '92')),
('Asnières-sur-Seine', 86512, (SELECT id FROM departement WHERE code = '92')),
('Colombes', 86650, (SELECT id FROM departement WHERE code = '92')),
('Rueil-Malmaison', 78152, (SELECT id FROM departement WHERE code = '92')),
('Issy-les-Moulineaux', 68395, (SELECT id FROM departement WHERE code = '92')),
('Levallois-Perret', 66407, (SELECT id FROM departement WHERE code = '92')),
('Neuilly-sur-Seine', 61768, (SELECT id FROM departement WHERE code = '92')),
('Antony', 61711, (SELECT id FROM departement WHERE code = '92')),
('Clichy', 61070, (SELECT id FROM departement WHERE code = '92')),

-- Orléans (45) - Département Loiret
('Orléans', 116238, (SELECT id FROM departement WHERE code = '45')),
('Fleury-les-Aubrais', 20228, (SELECT id FROM departement WHERE code = '45')),
('Olivet', 21275, (SELECT id FROM departement WHERE code = '45')),

-- Mulhouse (68) - Département Haut-Rhin
('Mulhouse', 108312, (SELECT id FROM departement WHERE code = '68')),
('Colmar', 69105, (SELECT id FROM departement WHERE code = '68')),
('Saint-Louis', 20087, (SELECT id FROM departement WHERE code = '68')),

-- Caen (14) - Département Calvados
('Caen', 105512, (SELECT id FROM departement WHERE code = '14')),
('Hérouville-Saint-Clair', 21031, (SELECT id FROM departement WHERE code = '14')),
('Lisieux', 20638, (SELECT id FROM departement WHERE code = '14')),
('Bayeux', 13348, (SELECT id FROM departement WHERE code = '14')),

-- Nancy (54) - Département Meurthe-et-Moselle
('Nancy', 104321, (SELECT id FROM departement WHERE code = '54')),
('Vandœuvre-lès-Nancy', 29378, (SELECT id FROM departement WHERE code = '54')),
('Lunéville', 18479, (SELECT id FROM departement WHERE code = '54')),

-- Saint-Denis (93) - Département Seine-Saint-Denis
('Saint-Denis', 112091, (SELECT id FROM departement WHERE code = '93')),
('Montreuil', 109914, (SELECT id FROM departement WHERE code = '93')),
('Aulnay-sous-Bois', 85740, (SELECT id FROM departement WHERE code = '93')),
('Aubervilliers', 88948, (SELECT id FROM departement WHERE code = '93')),
('Drancy', 71486, (SELECT id FROM departement WHERE code = '93')),
('Saint-Ouen-sur-Seine', 50494, (SELECT id FROM departement WHERE code = '93')),

-- Argenteuil (95) - Département Val-d'Oise
('Argenteuil', 110210, (SELECT id FROM departement WHERE code = '95')),
('Cergy', 64434, (SELECT id FROM departement WHERE code = '95')),
('Sarcelles', 59052, (SELECT id FROM departement WHERE code = '95')),
('Franconville', 34088, (SELECT id FROM departement WHERE code = '95')),

-- Avignon (84) - Département Vaucluse
('Avignon', 92209, (SELECT id FROM departement WHERE code = '84')),
('Carpentras', 28798, (SELECT id FROM departement WHERE code = '84')),
('Orange', 29135, (SELECT id FROM departement WHERE code = '84')),

-- Créteil (94) - Département Val-de-Marne
('Créteil', 91042, (SELECT id FROM departement WHERE code = '94')),
('Vitry-sur-Seine', 93569, (SELECT id FROM departement WHERE code = '94')),
('Saint-Maur-des-Fossés', 75035, (SELECT id FROM departement WHERE code = '94')),
('Ivry-sur-Seine', 60451, (SELECT id FROM departement WHERE code = '94')),
('Villejuif', 55983, (SELECT id FROM departement WHERE code = '94')),

-- Poitiers (86) - Département Vienne
('Poitiers', 88776, (SELECT id FROM departement WHERE code = '86')),
('Châtellerault', 31275, (SELECT id FROM departement WHERE code = '86')),

-- Versailles (78) - Département Yvelines
('Versailles', 85416, (SELECT id FROM departement WHERE code = '78')),
('Saint-Germain-en-Laye', 44753, (SELECT id FROM departement WHERE code = '78')),
('Sartrouville', 52315, (SELECT id FROM departement WHERE code = '78')),

-- La Rochelle (17) - Département Charente-Maritime
('La Rochelle', 76810, (SELECT id FROM departement WHERE code = '17')),
('Saintes', 25468, (SELECT id FROM departement WHERE code = '17')),
('Rochefort', 24014, (SELECT id FROM departement WHERE code = '17')),

-- Calais (62) - Département Pas-de-Calais
('Calais', 72509, (SELECT id FROM departement WHERE code = '62')),
('Arras', 41694, (SELECT id FROM departement WHERE code = '62')),
('Lens', 31131, (SELECT id FROM departement WHERE code = '62')),
('Béthune', 25068, (SELECT id FROM departement WHERE code = '62')),

-- Bourges (18) - Département Cher
('Bourges', 64668, (SELECT id FROM departement WHERE code = '18')),
('Vierzon', 26109, (SELECT id FROM departement WHERE code = '18')),

-- Lorient (56) - Département Morbihan
('Lorient', 57567, (SELECT id FROM departement WHERE code = '56')),
('Vannes', 54020, (SELECT id FROM departement WHERE code = '56')),
('Lanester', 22987, (SELECT id FROM departement WHERE code = '56')),

-- Niort (79) - Département Deux-Sèvres
('Niort', 58707, (SELECT id FROM departement WHERE code = '79')),
('Parthenay', 10540, (SELECT id FROM departement WHERE code = '79')),

-- Chambéry (73) - Département Savoie
('Chambéry', 59490, (SELECT id FROM departement WHERE code = '73')),
('Aix-les-Bains', 30011, (SELECT id FROM departement WHERE code = '73')),
('Albertville', 19214, (SELECT id FROM departement WHERE code = '73')),

-- Montauban (82) - Département Tarn-et-Garonne
('Montauban', 60810, (SELECT id FROM departement WHERE code = '82')),
('Castelsarrasin', 13619, (SELECT id FROM departement WHERE code = '82')),

-- Annecy (74) - Département Haute-Savoie
('Annecy', 132865, (SELECT id FROM departement WHERE code = '74')),
('Thonon-les-Bains', 35241, (SELECT id FROM departement WHERE code = '74')),
('Annemasse', 36250, (SELECT id FROM departement WHERE code = '74')),

-- Pau (64) - Département Pyrénées-Atlantiques
('Pau', 76666, (SELECT id FROM departement WHERE code = '64')),
('Bayonne', 51411, (SELECT id FROM departement WHERE code = '64')),
('Anglet', 39223, (SELECT id FROM departement WHERE code = '64')),
('Biarritz', 25404, (SELECT id FROM departement WHERE code = '64')),

-- Meaux (77) - Département Seine-et-Marne
('Meaux', 55750, (SELECT id FROM departement WHERE code = '77')),
('Chelles', 54234, (SELECT id FROM departement WHERE code = '77')),
('Melun', 40032, (SELECT id FROM departement WHERE code = '77'));

-- Affichage du résultat
SELECT 'Départements créés:' as info, COUNT(*) as nombre FROM departement
UNION ALL
SELECT 'Villes créées:' as info, COUNT(*) as nombre FROM ville;
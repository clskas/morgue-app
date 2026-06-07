# User Stories - Gestion de Morgue

## Épique 1 : Gestion des Défunts

### US-1.1 : Enregistrement d'un défunt
**En tant que** greffier,  
**Je veux** pouvoir enregistrer un défunt avec ses informations d'état civil,  
**Afin de** créer son dossier dans le système.

**Critères d'acceptation :**
- Saisie du nom, prénom, date de naissance, date de décès, lieu de décès
- Numéro de dossier unique généré automatiquement
- Pièces jointes possibles (documents scannés)
- Validation des champs obligatoires

### US-1.2 : Attribution d'un emplacement de stockage
**En tant que** greffier,  
**Je veux** attribuer un tiroir de conservation à un défunt,  
**Afin de** savoir où se trouve le corps.

**Critères d'acceptation :**
- Vue des emplacements disponibles/occupés
- Historique des changements d'emplacement
- Impossible d'assigner un emplacement déjà occupé

### US-1.3 : Recherche de défunt
**En tant que** utilisateur,  
**Je veux** rechercher un défunt par nom, prénom, date ou numéro de dossier,  
**Afin de** retrouver rapidement un dossier.

**Critères d'acceptation :**
- Recherche multicritères
- Autocomplétion sur le nom
- Résultats triés par défaut

## Épique 2 : Gestion des Interventions

### US-2.1 : Planification d'intervention
**En tant que** thanatopracteur,  
**Je veux** planifier une intervention de thanatopraxie,  
**Afin de** organiser mon travail.

**Critères d'acceptation :**
- Sélection du défunt
- Date et heure prévue
- Type d'intervention (conservation, soins de présentation)
- Statut (planifié, en cours, terminé)

### US-2.2 : Compte-rendu d'intervention
**En tant que** thanatopracteur,  
**Je veux** saisir le compte-rendu d'une intervention,  
**Afin de** documenter les actes réalisés.

**Critères d'acceptation :**
- Produits et quantités utilisés
- Observations
- Signature numérique
- Pièces jointes

## Épique 3 : Gestion des Sorties

### US-3.1 : Autorisation de sortie
**En tant que** médecin légiste,  
**Je veux** autoriser la sortie d'un corps,  
**Afin de** permettre son transport vers les pompes funèbres.

**Critères d'acceptation :**
- Vérification des documents obligatoires (certificat de décès, autorisation)
- Sélection du transporteur funéraire
- Signature numérique
- Horodatage

## Épique 4 : Mise à Jour Automatique

### US-4.1 : Vérification de mise à jour
**En tant que** utilisateur,  
**Je veux** être notifié automatiquement quand une mise à jour est disponible,  
**Afin de** bénéficier des dernières fonctionnalités et correctifs.

**Critères d'acceptation :**
- Vérification au démarrage de l'application
- Affichage d'une notification avec version et changelog
- Option de téléchargement immédiat ou de rappel plus tard
- Possibilité de désactiver temporairement

### US-4.2 : Installation de mise à jour
**En tant que** utilisateur,  
**Je veux** que la mise à jour se télécharge et s'installe automatiquement,  
**Afin de** ne pas perdre de temps.

**Critères d'acceptation :**
- Barre de progression du téléchargement
- Vérification de l'intégrité (checksum SHA-256)
- Installation après redémarrage de l'application
- Rollback possible en cas d'échec

## Épique 5 : Administration

### US-5.1 : Gestion des utilisateurs
**En tant que** administrateur,  
**Je veux** créer, modifier et désactiver des comptes utilisateurs,  
**Afin de** contrôler les accès au système.

**Critères d'acceptation :**
- Création avec rôle (Admin, Médecin, Thanatopracteur, Greffier)
- Réinitialisation de mot de passe
- Journal des connexions
- Désactivation (non-suppression)

### US-5.2 : Rapport d'activité
**En tant que** administrateur,  
**Je veux** générer des rapports d'activité,  
**Afin de** analyser le travail effectué.

**Critères d'acceptation :**
- Rapport par période
- Nombre d'entrées/sorties
- Interventions réalisées
- Export PDF

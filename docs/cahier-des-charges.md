# Cahier des Charges - Système de Gestion de Morgue

## 1. Présentation du Projet

### 1.1 Contexte
Développement d'une application desktop de gestion de morgue permettant le suivi complet des défunts, des opérations de thanatopraxie, des sorties de corps et des formalités administratives.

### 1.2 Objectifs
- Centraliser la gestion des défunts et des dossiers associés
- Assurer la traçabilité complète des mouvements de corps
- Automatiser les notifications de mises à jour
- Faciliter la recherche et le reporting

## 2. Fonctionnalités

### 2.1 Module Défunts
- Enregistrement des défunts avec données d'état civil
- Attribution d'un emplacement de stockage (tiroir/case)
- Suivi des entrées et sorties
- Historique des mouvements

### 2.2 Module Dossiers Médicaux
- Gestion des certificats de décès
- Suivi des autopsies
- Documents associés (scannés, PDF)

### 2.3 Module Thanatopraxie
- Planification des interventions
- Compte-rendu d'intervention
- Produits et matériels utilisés

### 2.4 Module Sorties
- Gestion des autorisations de sortie
- Suivi des transports funéraires
- Traçabilité des personnes autorisées

### 2.5 Module Utilisateurs
- Authentification sécurisée
- Gestion des rôles (Admin, Médecin, Thanatopracteur, Greffier)
- Journal d'audit

### 2.6 Module Mise à Jour Automatique
- Vérification de version au démarrage
- Notification de mise à jour disponible
- Téléchargement et installation automatique
- Vérification d'intégrité des fichiers

## 3. Contraintes Techniques

### 3.1 Stack Technique
- Langage : Java 17+
- UI : JavaFX 17+ avec FXML
- Build : Maven
- Base de données : H2 (embarquée) avec option PostgreSQL
- Packaging : JLink/JPackage (installateur natif)

### 3.2 Architecture
- Architecture en couches (MVC)
- DAO avec JPA/Hibernate
- Services métier distincts
- Threading pour les opérations lourdes

### 3.3 Sécurité
- Authentification par mot de passe hashé (BCrypt)
- Sessions utilisateur
- Journalisation des actions critiques
- Chiffrement des données sensibles

## 4. Livrables
- Application desktop installable (.exe/.msi)
- Code source avec documentation
- Scripts de déploiement
- Guide utilisateur
- Documentation technique vivante (ADRs)

## 5. Planification
- Phase 1 : Modèle de données et DAO
- Phase 2 : Services métier
- Phase 3 : Interface utilisateur
- Phase 4 : Système de mise à jour
- Phase 5 : Tests et déploiement

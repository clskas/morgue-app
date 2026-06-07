# ADR-003 : Système de mise à jour automatique

## Statut
Accepté

## Contexte
L'application doit pouvoir se mettre à jour automatiquement pour déployer les correctifs et nouvelles fonctionnalités.

## Décision
Implémentation d'un système client-serveur simple :
1. Fichier `version.json` hébergé sur un serveur HTTP
2. Vérification au démarrage de l'application
3. Téléchargement du JAR avec vérification SHA-256
4. Installation via script batch au redémarrage

## Conséquences
- L'utilisateur est notifié dès l'ouverture de l'application
- Intégrité des fichiers vérifiée avant installation
- Rollback possible via backup automatique
- Nécessite une connexion internet pour les mises à jour
- Update URL configurable dans Constants.java

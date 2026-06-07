# ADR-004 : Sécurité et Authentification

## Statut
Accepté

## Contexte
L'application manipule des données sensibles (dossiers de défunts) et nécessite un contrôle d'accès strict.

## Décision
- Hash des mots de passe avec BCrypt
- Session utilisateur en mémoire avec SessionManager
- Journalisation des actions critiques (AuditLog)
- Rôles : ADMIN, MEDECIN, THANATOPRACTEUR, GREFFIER

## Conséquences
- Protection contre les fuites de mots de passe
- Traçabilité complète des actions
- Contrôle d'accès granulaire
- Nécessité de gérer les sessions côté serveur

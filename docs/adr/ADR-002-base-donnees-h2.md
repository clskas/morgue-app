# ADR-002 : Base de données H2 embarquée

## Statut
Accepté

## Contexte
L'application doit fonctionner en autonomie sans nécessiter d'installation de serveur de base de données.

## Décision
Utilisation de H2 en mode embarqué comme base de données principale, avec possibilité de migration vers PostgreSQL.

## Conséquences
- Aucune installation serveur requise
- Données stockées dans le répertoire utilisateur (~/.gestionmorgue/db)
- Performances suffisantes pour un usage mono-poste
- Migration PostgreSQL possible via changement de dialecte Hibernate

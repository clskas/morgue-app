# ADR-001 : Architecture en couches (MVC)

## Statut
Accepté

## Contexte
Nécessité d'avoir une architecture modulaire et maintenable pour l'application de gestion de morgue.

## Décision
Adoption d'une architecture MVC en 4 couches :
1. **Vue** (JavaFX FXML) : Interface utilisateur
2. **Contrôleur** : Gestion des événements utilisateur
3. **Service** : Logique métier
4. **DAO** : Accès aux données (JPA/Hibernate)

## Conséquences
- Séparation claire des responsabilités
- Testabilité améliorée
- Possibilité de changer la couche UI sans impacter le métier
- Complexité accrue mais meilleure maintenabilité

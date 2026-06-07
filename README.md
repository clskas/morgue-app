# Gestion Morgue

Application desktop de gestion de morgue avec mise à jour automatique.

## Stack technique
- **Java 17+** / **JavaFX 17+** (UI)
- **Maven** (Build)
- **H2** / **JPA Hibernate** (Persistance)
- **BCrypt** (Sécurité)

## Prérequis
- JDK 17+
- Maven 3.8+

## Installation
```bash
mvn clean package
java -jar target/gestionmorgue.jar
```

## Structure du projet
```
gestionmorgue/
├── docs/
│   ├── cahier-des-charges.md
│   ├── user-stories.md
│   ├── architecture.md
│   └── adr/            # Architecture Decision Records
├── src/
│   ├── main/java/com/gestionmorgue/
│   │   ├── App.java
│   │   ├── config/
│   │   ├── model/
│   │   ├── dao/
│   │   ├── service/
│   │   ├── controller/
│   │   └── update/
│   └── main/resources/
│       ├── views/
│       └── styles/
└── pom.xml
```

## Mise à jour
Le système vérifie automatiquement les mises à jour au démarrage.
URL de mise à jour configurable dans `config/Constants.java`.

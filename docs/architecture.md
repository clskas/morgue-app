# Architecture Logicielle

## Vue d'ensemble

```
┌─────────────────────────────────────────────────┐
│                  JavaFX UI (MVC)                │
├─────────────────────────────────────────────────┤
│              Services Métier                    │
├─────────────────────────────────────────────────┤
│     DAO (JPA / Hibernate)                       │
├─────────────────────────────────────────────────┤
│  Base de données (H2 / PostgreSQL)              │
└─────────────────────────────────────────────────┘
```

## Structure du Projet

```
com.gestionmorgue
├── App.java                      # Point d'entrée
├── config/                       # Configuration
│   ├── ConfigManager.java
│   ├── DatabaseConfig.java
│   └── Constants.java
├── model/                        # Entités JPA
│   ├── Deceased.java
│   ├── StorageLocation.java
│   ├── Intervention.java
│   ├── ExitAuthorization.java
│   ├── User.java
│   └── AuditLog.java
├── dao/                          # Data Access Objects
│   ├── GenericDao.java
│   ├── DeceasedDao.java
│   ├── StorageLocationDao.java
│   ├── InterventionDao.java
│   └── UserDao.java
├── service/                      # Services métier
│   ├── DeceasedService.java
│   ├── InterventionService.java
│   ├── StorageService.java
│   ├── AuthService.java
│   └── ReportService.java
├── controller/                   # Contrôleurs JavaFX
│   ├── LoginController.java
│   ├── DashboardController.java
│   ├── DeceasedController.java
│   └── UpdateController.java
├── update/                       # Mise à jour automatique
│   ├── UpdateChecker.java
│   ├── UpdateDownloader.java
│   └── UpdateInstaller.java
└── util/                         # Utilitaires
    ├── DatabaseManager.java
    ├── SessionManager.java
    └── NotificationUtil.java
```

## Flux de mise à jour automatique

```
Démarrage App
    │
    ▼
Vérification version (HTTP)
    │
    ├── Nouvelle version ▼
    │   └── Notification utilisateur
    │       ├── Télécharger maintenant
    │       │   └── Download → Vérification SHA-256
    │       │       └── Appliquer au redémarrage
    │       └── Plus tard (notif au prochain démarrage)
    │
    └── Version à jour → Dashboard
```

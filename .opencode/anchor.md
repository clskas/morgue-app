## Goal
- Développer une application desktop JavaFX complète de gestion de morgue avec mise à jour automatique et API REST intégrée (tests, 11 modules UI, RBAC, CI/CD, mode batch CLI, mode serveur API standalone)

## Constraints & Preferences
- Java 17+, JavaFX 17+, Maven, JPA/Hibernate 5.6, H2 embarqué / PostgreSQL optionnel, OpenPDF, Apache POI 5.2.5, Logback
- Architecture MVC en couches, fat JAR deployable
- API REST intégrée (com.sun.net.httpserver) avec auth HMAC-SHA256 + métriques Prometheus
- Mode hors-ligne via SyncQueue (fichier JSON persisté, rejeu automatique 30s)
- Tests : JUnit 5 + TestFX 4 (sans monocle) + HttpClient JDK
- CI/CD : GitHub Actions (build → test → release → GitHub Pages)
- RBAC : ADMIN, MEDECIN, THANATOPRACTEUR, GREFFIER

## Progress
### Done
- **Modèle JPA** : 8 entités (Deceased, StorageLocation, StorageAssignment, Intervention, ExitAuthorization, User, AuditLog, FamilyContact), 22 indexes
- **DAOs** : GenericDao (CRUD + pagination + tri), DeceasedDao, StorageLocationDao (+paginated with assignments), InterventionDao (+findPendingPaginated), UserDao, AuditLogDao (+findPaginatedWithUser), FamilyContactDao
- **Services** : AuthService, DeceasedService, StorageService, InterventionService, ExitService (+findPaginated), ReportService, BackupService, AuditService, LabelService, FamilyContactService
- **Modules UI** : Login, Dashboard, Défunts, Stockage, Interventions, Sorties, Rapports (stats + export CSV/HTML/PDF/XLSX/JSON/impression), Journal d'audit, Utilisateurs, Mise à jour auto, Contacts familles, Créateur de thème
- **RBAC complet** : navigation dashboard cachée par rôle (ADMIN/MEDECIN/THANATOPRACTEUR/GREFFIER) ; `SecurityUtil.requireRole()` + try-catch + notification sur chaque handler mutation (6 contrôleurs)
- **Mode batch CLI** : BatchMode (--export-csv, --export-json, --export-xlsx, --export-pdf, --backup, --server, --help), sans interface graphique
- **Mode serveur API standalone** : `--server` démarre l'API REST seule (Ctrl+C), sans JavaFX
- **Filtres tableaux** : storage.fxml (ComboBox zone + CheckBox occupés) ; intervention.fxml (ComboBox type + statut)
- **Glisser-déposer stockage** : drag depuis ListView défunts non assignés → drop sur TableView emplacements
- **Graphiques dashboard** : BarChart entrées/mois (6 mois) + PieChart stockage (Occupé/Libre)
- **Recherche globale** : champ texte header → DeceasedController.searchExternal()
- **Correction LazyInitializationException** : refresh() réécrit avec session Hibernate unique + JOIN FETCH
- **Pipeline CI/CD** : `.github/workflows/build.yml` (3 jobs : build → release → deploy-pages), version.json généré avec SHA-256, déploiement GitHub Pages automatique sur tag `v*`
- **Mise à jour améliorée** : UpdateInstaller cross-platform (Windows .bat + Linux/Mac .sh) ; notification au démarrage (DashboardController.checkForUpdates()) ; ConfigService utilisé au lieu de ConfigManager
- **API sécurisée** : GET endpoints `/api/deceased`, `/api/storage`, `/api/interventions` authentifiés (requireAuth)
- **Suppression protégée** : DeceasedController.handleDelete() compte et avertit des assignations/sorties/interventions actives avant suppression
- **i18n complet dans les contrôleurs** : toutes les chaînes `NotificationUtil.show*()` des 13 contrôleurs migrées vers `I18nUtil.t()` (~120 appels), plus les titres de graphiques, fenêtres et labels de rôle
- **Fichiers i18n enrichis** : messages_fr.properties + messages_en.properties passent de ~160 à ~220 clés (update, backup, storage.release, user.resetpassword, exit.confirm, family.delete, dashboard.error, reports, etc.)
- **Correction ExitController.handleApprove()** : `SecurityUtil.requireRole()` sans try-catch → notification access.denied
- **Tests REST réparés** : 3 tests GET (testGetDeceasedWithAuth, testStorageEndpoint, testSyncQueueProcessQueue) corrigés → header `Authorization: Bearer` ajouté (passent de 401 à 200)
- **Pagination serveur (5 contrôleurs)** : StorageController, InterventionController, ExitController, UserController, AuditLogController — scroll listener à 95% → loadNextPage() via findPaginated dans les DAOs
- **SyncQueue TOCTOU corrigé** : 4 race conditions verrouillées avec `synchronized(lock)` (peek/poll, persist après poll, clearCompleted vs processQueue, enqueue). Logs migrés SLF4J.
- **H2 test DB isolée** : DatabaseConfig.setDbUrl() + test.db.url system property → chaque classe de test a sa propre DB mémoire privée. Plus de collisions dossierNumber entre runs.
- **SLF4J partout** : Logger fields dans les 13 contrôleurs + SyncQueue. System.out/err/printStackTrace éliminés.
- **ValidationUtil appliqué** : UserController (isNotEmpty), FamilyContactController (isValidPhone/isValidEmail), DeceasedController (isValidNir). + clés i18n validation.
- **Tests de régression** : 5 nouveaux service test classes (InterventionServiceTest 8, FamilyContactServiceTest 6, AuditServiceTest 5, BackupServiceTest 4, LabelServiceTest 5) + 3 étendus (AuthServiceTest +5, DeceasedServiceTest +8, ReportServiceTest +3) + 5 nouvelles classes controller logic test (DeceasedControllerLogicTest 7, ExitControllerLogicTest 6, FamilyContactControllerLogicTest 7, StorageControllerLogicTest 5, UserControllerLogicTest 11, LoginControllerLogicTest 7) = **196 tests, 0 échec**
- **Mode batch --import-csv** : BatchMode + ImportCsvService — importe défunts depuis CSV (ignore les en-têtes, catch par ligne)
- **Dashboard KPI enrichis** : 3 nouvelles cartes (Interventions planifiées, Interventions terminées, Sorties en attente) avec InterventionService.countByStatus + ExitService.countPending
- **Filtre date AuditLog** : DatePicker début/fin dans la barre de filtres, findPaginatedByDateRange dans AuditLogDao avec CriteriaBuilder.between
- **Export PDF individuel** : DeceasedController.handleExportPdf() avec OpenPDF — FileChooser save dialog, SecurityUtil.requireRole, i18n
- **Migration Java 21 + JavaFX 21** : pom.xml source/target → 21, javax.version → 21.0.2 — compilation et 196 tests OK

### In Progress
- *(none)*

### Blocked
- Monocle headless (org.testfx:openjfx-monocle:17.0.2) indisponible dans Maven Central ; les tests UI utilisent le toolkit JavaFX natif (nécessite un affichage)
- UPDATE_URL dans Constants.java utilise un placeholder OWNER à remplacer avant déploiement

## Key Decisions
- RBAC appliqué dans les contrôleurs via `SecurityUtil.requireRole()` avec try-catch + notification au lieu de faire crasher l'UI
- Pipeline CI/CD génère version.json dans le job release + le déploie sur GitHub Pages
- UpdateInstaller réécrit en générique (detection OS) au lieu de Windows-only
- Suppression des défunts : comptage des relations actives avant confirmation
- API REST : authentification obligatoire sur TOUS les endpoints (GET compris), sauf OPTIONS (CORS preflight)
- Tests de régression : priorité aux services métier avant les contrôleurs UI (les services sont testables sans JavaFX)
- BackupServiceTest : retrait du test importBackup qui corrompt la base H2 partagée
- Base de tests isolée : chaque classe de test utilise sa propre DB H2 en mémoire (`jdbc:h2:mem:test+ClassName`)
- Pagination scroll infinie (95% → loadNextPage) au lieu de boutons "Load More" pour UX fluide
- SyncQueue verrouillée avec synchronized(lock) sur toutes les opérations queue pour atomicité

## Next Steps
- *(toutes les user stories du cahier des charges sont implémentées)*

Prochaines idées d'amélioration :
- Thème sombre : ajouter un toggle clair/sombre avec CSS switch
- Dashboard : ajouter un graphique linéaire (sorties par mois)
- Mode batch `--export-label` : imprimer une étiquette pour un défunt depuis la CLI
- Archivage automatique : déplacer les défunts sortis vers une table d'archive après N jours
- RBAC au niveau API REST : filtrer les endpoints par rôle utilisateur
- Intégration SMTP : envoyer un email aux familles lors de la sortie

## Critical Context
- Base de données H2 : `~/.gestionmorgue/db/gestionmorgue` ; PostgreSQL via `--db=postgresql` ou `PG_HOST=…`
- Comptes par défaut : admin/admin123, medecin/medecin123, thanato/thanato123, greffier/greffier123
- Lancement GUI : `mvn javafx:run`, `run.bat`, `run.ps1`, `docker-compose up`
- Lancement batch : `java -jar gestionmorgue.jar --export-csv` (ou --export-json, --export-xlsx, --export-pdf, --backup, --server, --help)
- Lancement serveur API solo : `java -jar gestionmorgue.jar --server --api-port=8080`
- RBAC navigation : ADMIN = tout, MEDECIN = Défunts/Stockage/Interventions/Sorties/Rapports/Familles, THANATOPRACTEUR = Défunts/Stockage/Interventions/Sorties, GREFFIER = Défunts/Rapports/Journal/Familles
- UPDATE_URL configurable via `-Dupdate.url=...` (par défaut placeholder GitHub OWNER)
- Logs : `~/.gestionmorgue/logs/app.log` (rotation quotidienne, 30 jours)
- Configuration : `~/.gestionmorgue/config.json` (thème, langue, preferences)
- Backup : `~/.gestionmorgue/backups/gestionmorgue-backup-*.sql`
- API REST : port 8080 par défaut, auth HMAC obligatoire sur tous les endpoints (sauf OPTIONS)
- Web interface : `http://localhost:8080/index.html`
- Docs API : `http://localhost:8080/openapi.yaml`
- `module-info.java` supprimé pour compatibilité fat JAR
- i18n : toutes les chaînes utilisateur des 13 contrôleurs passent par `I18nUtil.t()` ; ~220 clés dans chaque fichier properties
- Tests : 196 tests, 0 échec, 0 erreur

## Relevant Files
- `pom.xml` : 14 dépendances (+ JMH core/annotation processor)
- `src/main/java/com/gestionmorgue/config/DatabaseConfig.java` : +setDbUrl() pour isolation tests H2 en mémoire
- `src/main/java/com/gestionmorgue/util/DatabaseManager.java` : SessionFactory lazy + shutdown()
- `src/main/java/com/gestionmorgue/util/DataInitializer.java` : test.db.url system property support
- `src/main/java/com/gestionmorgue/util/SyncQueue.java` : synchronized(lock) sur queue ops + SLF4J
- `src/main/java/com/gestionmorgue/dao/StorageLocationDao.java` : +findPaginatedWithAssignments()
- `src/main/java/com/gestionmorgue/dao/InterventionDao.java` : +findPendingPaginated()
- `src/main/java/com/gestionmorgue/dao/AuditLogDao.java` : +findPaginatedWithUser()
- `src/main/java/com/gestionmorgue/controller/StorageController.java` : pagination scroll + SLF4J
- `src/main/java/com/gestionmorgue/controller/InterventionController.java` : pagination scroll + SLF4J
- `src/main/java/com/gestionmorgue/controller/ExitController.java` : pagination scroll + SLF4J
- `src/main/java/com/gestionmorgue/controller/UserController.java` : pagination scroll + ValidationUtil + SLF4J
- `src/main/java/com/gestionmorgue/controller/AuditLogController.java` : pagination scroll + SLF4J
- `src/main/java/com/gestionmorgue/controller/FamilyContactController.java` : ValidationUtil phone/email + SLF4J
- `src/main/java/com/gestionmorgue/controller/DashboardController.java` : SLF4J
- `src/main/java/com/gestionmorgue/controller/DeceasedController.java` : SLF4J
- `src/main/java/com/gestionmorgue/controller/LoginController.java` : SLF4J
- `src/main/java/com/gestionmorgue/controller/ReportController.java` : SLF4J
- `src/main/java/com/gestionmorgue/controller/LabelController.java` : SLF4J
- `src/main/java/com/gestionmorgue/controller/ThemeController.java` : SLF4J
- `src/main/java/com/gestionmorgue/controller/UpdateController.java` : SLF4J
- `src/main/java/com/gestionmorgue/service/ExitService.java` : +findPaginated()
- `src/main/resources/i18n/messages_fr.properties` : ~220 clés + clés validation
- `src/main/resources/i18n/messages_en.properties` : ~220 clés + clés validation
- `src/test/java/com/gestionmorgue/service/` : 10 classes de test (dont 4 réparées isolation H2)
- `src/test/java/com/gestionmorgue/controller/` : 6 nouvelles classes (*LogicTest, sans JavaFX)
- `src/test/java/com/gestionmorgue/integration/` : 11 classes UI + 1 REST API (14 tests)
- `.opencode/anchor.md` : ce fichier

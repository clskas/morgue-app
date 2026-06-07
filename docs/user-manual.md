# Manuel Utilisateur — Gestion Morgue

Application desktop de gestion de morgue avec mise à jour automatique,
API REST intégrée et mode batch CLI.

---

## Table des matières

1. [Installation et lancement](#1-installation-et-lancement)
2. [Authentification et RBAC](#2-authentification-et-rbac)
3. [Dashboard](#3-dashboard)
4. [Gestion des défunts](#4-gestion-des-défunts)
5. [Gestion du stockage](#5-gestion-du-stockage)
6. [Interventions](#6-interventions)
7. [Sorties](#7-sorties)
8. [Rapports](#8-rapports)
9. [Journal d'audit](#9-journal-daudit)
10. [Gestion des utilisateurs](#10-gestion-des-utilisateurs)
11. [Contacts familles](#11-contacts-familles)
12. [Étiquettes](#12-étiquettes)
13. [Thème](#13-thème)
14. [Mise à jour automatique](#14-mise-à-jour-automatique)
15. [Mode batch CLI](#15-mode-batch-cli)
16. [API REST](#16-api-rest)
17. [Configuration base de données](#17-configuration-base-de-données)

---

## 1. Installation et lancement

### Prérequis

- **Java 21** ou supérieur
- Optionnel : Maven 3.8+ (pour compilation depuis les sources)

### Téléchargement

```bash
# Depuis GitHub Releases
curl -LO https://github.com/clskas/morgue-app/releases/latest/download/gestionmorgue-1.0.0.jar
```

### Lancement

```bash
# Interface graphique
java -jar gestionmorgue-1.0.0.jar

# Ou avec run.bat (Windows) / run.ps1 (PowerShell)
```

### Modes alternatifs

```bash
# Mode serveur API seul (sans interface graphique)
java -jar gestionmorgue-1.0.0.jar --server

# Mode batch CLI
java -jar gestionmorgue-1.0.0.jar --help
```

### Construction depuis les sources

```bash
git clone https://github.com/clskas/morgue-app.git
cd morgue-app
mvn clean package -DskipTests
java -jar target/gestionmorgue-1.0.0.jar
```

### Docker

```bash
docker-compose up
# ou
docker build -t gestionmorgue .
docker run -p 8080:8080 gestionmorgue
```

---

## 2. Authentification et RBAC

### Connexion

La fenêtre de connexion demande un **nom d'utilisateur** et un **mot de passe**.

### Rôles disponibles

| Rôle | Accès |
|------|-------|
| **ADMIN** | Tous les modules, gestion des utilisateurs |
| **MEDECIN** | Défunts, Stockage, Interventions, Sorties, Rapports, Familles |
| **THANATOPRACTEUR** | Défunts, Stockage, Interventions, Sorties |
| **GREFFIER** | Défunts, Rapports, Journal d'audit, Familles |

### Comptes par défaut

| Utilisateur | Mot de passe | Rôle |
|-------------|--------------|------|
| `admin` | `admin123` | ADMIN |
| `medecin` | `medecin123` | MEDECIN |
| `thanato` | `thanato123` | THANATOPRACTEUR |
| `greffier` | `greffier123` | GREFFIER |

### Navigation

La barre de navigation latérale affiche uniquement les modules autorisés
pour le rôle connecté. Les actions non autorisées affichent une notification
d'erreur.

---

## 3. Dashboard

Le tableau de bord affiche des indicateurs clés (KPI) :

- **Défunts enregistrés** (total)
- **Stockage occupé / libre** (capacité)
- **Interventions planifiées / terminées**
- **Sorties en attente**

### Graphiques

- **BarChart** : Entrées par mois (6 derniers mois)
- **PieChart** : Répartition stockage (Occupé / Libre)

Le dashboard est accessible à tous les rôles.

---

## 4. Gestion des défunts

### Enregistrement d'un défunt

Formulaire avec les champs :
- **NIR** (numéro INSEE) — validation automatique
- **Nom** (obligatoire)
- **Prénom**
- **Date de naissance**
- **Date de décès** (obligatoire)
- **Lieu de décès**
- **Numéro de dossier** (généré automatiquement)

### Liste des défunts

Tableau paginé (défilement infini : charge la page suivante à 95% du scroll).
Tri par clic sur les colonnes.

### Recherche

Champ de recherche avec **autocomplétion** (suggestions après 2 caractères).
Recherche multicritères (nom, prénom, NIR, numéro dossier).

### Pièces jointes

- Onglet "Pièces jointes" dans la fiche défunt
- Ajout par glisser-déposer ou bouton "Ajouter"
- Téléchargement et suppression des fichiers
- Stockage sur disque : `~/.gestionmorgue/attachments/DECEASED/{id}/`

### Export PDF individuel

Bouton "Exporter PDF" dans la fiche défunt — génère un document OpenPDF
avec les informations du défunt.

---

## 5. Gestion du stockage

### Vue d'ensemble

Tableau paginé des emplacements avec :
- Zone (réfrigérateur, scellé, etc.)
- Capacité et occupation
- Défunts assignés
- Statut (Libre / Occupé)

### Filtres

- Par **zone**
- Par statut **occupé / libre**

### Attribution d'un emplacement

- Sélectionner un défunt dans la liste déroulante
- Choisir un emplacement libre
- Cliquer "Attribuer"

### Glisser-déposer

Faire glisser un défunt depuis la liste vers l'emplacement souhaité
dans le tableau.

### Historique

Onglet "Historique" : tableau chronologique de toutes les
assignations/libérations avec le nom du défunt, l'emplacement,
les dates et l'utilisateur ayant libéré.

### Drag & Drop

- Drag un défunt depuis la ListView → drop sur un emplacement dans le TableView
- L'assignation est créée automatiquement

---

## 6. Interventions

### Planification

- Sélectionner un défunt
- Choisir le **type** (Thanatopraxie, Soins de conservation, Autopsie,
Prélèvement, Toilette mortuaire, Habillage)
- Définir la **date prévue**
- Statut initial : PLANIFIEE

### Liste des interventions

Tableau paginé avec défilement infini.

### Filtres

- Par **type** d'intervention
- Par **statut** (PLANIFIEE, EN_COURS, TERMINEE, ANNULEE)

### Compte-rendu

- Saisir les produits utilisés et observations
- **Signature numérique** : horodatage + nom de l'utilisateur
(champ obligatoire pour terminer)
- **Pièces jointes** : photos ou documents liés à l'intervention

### Changement de statut

- Démarrer → EN_COURS
- Terminer → TERMINEE (signature requise)
- Annuler → ANNULEE

---

## 7. Sorties

### Création d'une autorisation de sortie

- Sélectionner un défunt
- Renseigner le transporteur
- Date et heure de sortie

### Liste des sorties

Tableau paginé avec :
- Défunt, transporteur, date, statut
- Statut (EN_ATTENTE, APPROUVEE, REFUSEE)

### Vérification documents (US-3.1)

Trois cases à cocher obligatoires avant approbation :
- [ ] Certificat de décès
- [ ] Autorisation de sortie
- [ ] Pièce d'identité

Notes additionnelles possibles.

### Approbation

- **Signature numérique** obligatoire (nom + horodatage)
- Documents requis cochés → bouton "Approuver" activé

### Refus

- Motif de refus obligatoire
- Signature requise

---

## 8. Rapports

### Génération de rapport

Période au choix (début/fin).

### Contenu

- Entrées (enregistrements de défunts)
- Sorties (autorisations approuvées)
- Interventions (terminées)
- Statistiques globales

### Formats d'export

| Format | Extension | Bouton |
|--------|-----------|--------|
| PDF | `.pdf` | Exporter PDF |
| CSV | `.csv` | Exporter CSV |
| XLSX | `.xlsx` | Exporter Excel |
| JSON | `.json` | Exporter JSON |

### Utilisation

Le rapport est affiché dans l'interface avant export.
Tous les formats sont générés avec un FileChooser pour choisir
l'emplacement de sauvegarde.

---

## 9. Journal d'audit

### Consultation

Tableau paginé de toutes les actions utilisateur :
- Date/heure
- Utilisateur
- Action
- Détails
- Adresse IP

### Filtres

- **Filtre par date** : DatePicker début + DatePicker fin
- Recherche textuelle

### Accès

Réservé aux rôles **ADMIN** et **GREFFIER**.

---

## 10. Gestion des utilisateurs

### Liste

Tableau paginé des utilisateurs avec :
- Nom d'utilisateur
- Rôle
- Statut (Actif / Inactif)
- Date de création

### Création / Modification

- Nom d'utilisateur
- Mot de passe (avec confirmation)
- Rôle (ADMIN, MEDECIN, THANATOPRACTEUR, GREFFIER)

### Réinitialisation de mot de passe

Bouton "Réinitialiser" sur chaque utilisateur.
Nouveau mot de passe généré/saisi.

### Désactivation

Un utilisateur peut être marqué Inactif (il ne peut plus se connecter).

### Accès

Réservé au rôle **ADMIN** uniquement.

---

## 11. Contacts familles

### Gestion des contacts

- Ajout d'un contact famille pour un défunt
- Champs : nom, lien de parenté, téléphone, email
- **Validation** : email valide, téléphone valide

### Liste

Tableau paginé lié au défunt sélectionné.

### Accès

Rôles **ADMIN**, **MEDECIN** et **GREFFIER**.

---

## 12. Étiquettes

### Impression

- Saisir le nom du défunt
- Générer une étiquette avec code-barres
- Bouton "Imprimer"

### Format

Étiquette formatée pour impression sur support standard.

---

## 13. Thème

### Créateur de thème

Personnalisation des couleurs de l'interface :
- Couleur primaire
- Couleur secondaire
- Couleur de fond
- Couleur du texte
- Couleur des boutons

### Aperçu en direct

Les modifications sont visibles en temps réel.

### Sauvegarde

Le thème personnalisé est persisté dans la configuration
et appliqué au prochain démarrage.

---

## 14. Mise à jour automatique

### Vérification

Au démarrage, l'application vérifie la dernière version disponible
sur GitHub Releases.

### Notification

Si une mise à jour est disponible, une notification s'affiche
avec le numéro de version et le changelog.

### Installation

1. Cliquer "Mettre à jour"
2. Barre de progression
3. Vérification SHA-256 du fichier téléchargé
4. Redémarrage automatique

### Rollback (US-4.2)

- Avant mise à jour : **backup** du JAR courant dans
`~/.gestionmorgue/backups/gestionmorgue-backup-*.jar`
- En cas d'échec : bouton **Restaurer** dans l'interface
- L'ancienne version est restaurée depuis le backup

### Configuration

URL de mise à jour configurable via :

```bash
java -jar gestionmorgue.jar -Dupdate.url=https://...
```

---

## 15. Mode batch CLI

L'application peut être utilisée sans interface graphique :

```bash
# Aide
java -jar gestionmorgue-1.0.0.jar --help

# Export CSV des défunts
java -jar gestionmorgue-1.0.0.jar --export-csv

# Export JSON
java -jar gestionmorgue-1.0.0.jar --export-json

# Export Excel XLSX
java -jar gestionmorgue-1.0.0.jar --export-xlsx

# Export PDF
java -jar gestionmorgue-1.0.0.jar --export-pdf

# Backup base de données (SQL)
java -jar gestionmorgue-1.0.0.jar --backup

# Import CSV
java -jar gestionmorgue-1.0.0.jar --import-csv

# Serveur API seul (sans GUI)
java -jar gestionmorgue-1.0.0.jar --server
```

---

## 16. API REST

L'API REST intégrée écoute sur le port **8080**.

### Authentification HMAC

Chaque requête doit inclure :
- `X-Auth-Key` : clé API
- `X-Auth-Signature` : HMAC-SHA256 de la requête
- `X-Auth-Timestamp` : timestamp Unix

### Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/deceased` | Liste des défunts |
| GET | `/api/storage` | État du stockage |
| GET | `/api/interventions` | Liste des interventions |
| GET | `/api/metrics` | Métriques Prometheus |

### Interface web

- `http://localhost:8080/index.html` — interface web de test
- `http://localhost:8080/openapi.yaml` — documentation OpenAPI
- `http://localhost:8080/metrics` — métriques Prometheus

---

## 17. Configuration base de données

### H2 embarqué (par défaut)

Base de données locale dans `~/.gestionmorgue/db/gestionmorgue`.

### PostgreSQL

```bash
java -jar gestionmorgue.jar --db=postgresql
```

Ou via variables d'environnement :
- `DB_URL=jdbc:postgresql://localhost:5432/gestionmorgue`
- `DB_USER=gestionmorgue`
- `DB_PASSWORD=secret`

### Docker Compose

```bash
docker-compose up
```

Démarre PostgreSQL + l'application automatiquement.

### Backup base

```bash
java -jar gestionmorgue.jar --backup
```

Génère un fichier SQL dans `~/.gestionmorgue/backups/`.

---

## Dépannage

### Logs

`~/.gestionmorgue/logs/app.log` (rotation quotidienne, 30 jours)

### Configuration

`~/.gestionmorgue/config.json`

### Problème de connexion

Réinitialiser le mot de passe admin via la base H2 :
```bash
java -jar gestionmorgue.jar
```
Les comptes par défaut sont recréés si la base est vide.

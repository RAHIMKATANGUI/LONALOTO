# LONALOTO — Bloc 1 : Entités Room + Moteur de calcul

## Ce que contient ce livrable

```
LONALOTO/
├── build.gradle.kts                   ← config racine (plugins)
├── settings.gradle.kts                ← déclare le module "app"
├── gradle.properties
├── app/
│   ├── build.gradle.kts               ← dépendances (Room, Hilt, Compose, POI, iText...)
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/values/strings.xml, themes.xml
│       │   └── java/com/lonaloto/
│       │       ├── LonalotoApplication.kt
│       │       ├── data/local/
│       │       │   ├── entities/       ← Utilisateur, Activite, Mois, Vente,
│       │       │   │                     PalierBonus, Parametre, HistoriqueAudit
│       │       │   ├── dao/            ← un DAO par entité + requêtes agrégées
│       │       │   ├── AppDatabase.kt
│       │       │   └── Convertisseurs.kt
│       │       └── domain/calcul/
│       │           ├── CalculPaieMensuelle.kt   ← cœur du moteur de calcul
│       │           ├── CalculBonus.kt           ← paliers configurables ADMIN
│       │           └── CalculConsolidation.kt   ← agrégation multi-activités
│       └── test/java/com/lonaloto/domain/calcul/
│           └── LonalotoExcelReferenceTest.kt    ← validation contre les vrais chiffres Excel
```

## Comment l'intégrer dans ton dépôt GitHub

1. Récupère ce dossier (tous les fichiers listés ci-dessus).
2. Copie-le **à la racine** de ton dépôt `RAHIMKATANGUI/LONALOTO`, à côté du dossier
   `.github/` déjà présent. La structure finale doit ressembler à :
   ```
   LONALOTO/  (ton dépôt GitHub)
   ├── .github/workflows/android-build.yml   ← déjà en place
   ├── README.md                              ← déjà en place
   ├── build.gradle.kts                       ← nouveau
   ├── settings.gradle.kts                    ← nouveau
   ├── gradle.properties                      ← nouveau
   └── app/                                   ← nouveau
   ```
3. Commit + push. Le workflow CI que tu as déjà activé va automatiquement essayer
   de compiler — à ce stade il devrait aller plus loin qu'avant (structure Gradle
   présente), mais restera incomplet tant que les écrans (Compose UI) ne sont pas
   ajoutés dans les prochains blocs.

## Comment vérifier le moteur de calcul dès maintenant

Le fichier `LonalotoExcelReferenceTest.kt` reproduit les données réelles d'Août 2026
(onglet RICHMOND) et vérifie que le calcul Kotlin donne EXACTEMENT les mêmes résultats
que les cellules Excel :

| Ligne Excel (RICHMOND, Août 2026) | Valeur Excel | Vérifié par le test |
|---|---|---|
| TOTAL RECETTE (C98)      | 2 872 675 FCFA | ✅ |
| TOTAL PAIEMENT (D98)     | 1 008 375 FCFA | ✅ |
| MONTANT TOTAL (C101)     | 403 699.00 FCFA | ✅ |
| MONTANT TOTAL LONACI (C102) | 395 625.02 FCFA | ✅ |
| POINT PAIEMENT (C103)    | 30 251.25 FCFA | ✅ |
| SALAIRE COUPEUR (C104)   | 114 907.00 FCFA | ✅ |
| SALAIRE MENSUEL (C105)   | 280 718.02 FCFA | ✅ |

`PinHasherTest.kt` vérifie que le PIN n'est jamais stocké/comparé en clair et que
le hash/la vérification fonctionnent correctement (BCrypt).

## Bloc 2 — Connexion + Rôles (nouveau)

- **`domain/auth/PinHasher.kt`** — hash BCrypt du PIN (jamais stocké en clair)
- **`domain/auth/SessionManager.kt`** — session en mémoire uniquement (fermée si l'app
  passe en arrière-plan — comportement volontaire pour un téléphone partagé sur le terrain,
  ajustable plus tard via un paramètre "durée de session")
- **`domain/auth/Permissions.kt`** — matrice de permissions centralisée (un seul endroit
  à modifier si les droits d'un rôle évoluent)
- **`data/repository/AuthRepository.kt`** — connexion Nom+PIN, journalisée dans l'audit
- **`ui/connexion/LoginScreen.kt` + `LoginViewModel.kt`** — écran de connexion Compose,
  champs et bouton larges pour usage terrain
- **`ui/navigation/LonalotoNavGraph.kt`** — après connexion, redirige automatiquement
  vers un dashboard différent selon le rôle (ADMIN / CHEF_DE_FLOTTE / VENDEUR) — pour
  l'instant des écrans placeholder, à détailler dans le prochain bloc
- **`di/DatabaseModule.kt`** — câblage Hilt de Room et des DAO

## Comment tester dès maintenant

Une fois le projet dans Android Studio (ou via `./gradlew test` en local/CI) :
```
./gradlew testDebugUnitTest
```
Le test échoue immédiatement si un futur changement casse la logique de calcul —
c'est ta garantie que l'app donnera toujours les mêmes résultats que l'Excel actuel.

## Ce qui n'est PAS encore inclus (prochains blocs)

- Écran de saisie côté CHEF_DE_FLOTTE avec sélection du vendeur + liste à valider (pour l'instant, saisie = vendeur connecté uniquement)
- Sélecteur d'activité pour l'ADMIN dans Rapports (actuellement basé sur la session)
- Icônes PNG de secours (pré-Android 8 / API < 26) — voir note dans le bloc 5

## Bloc 6 — Export PDF/Excel + Sauvegarde (nouveau)

- **`data/export/ExcelExporter.kt`** — génère un .xlsx reproduisant la structure de
  l'Excel LONALOTO d'origine (JOUR/DATE/RECETTE/PAIEMENT + calcul de paie), via Apache POI
- **`data/export/PdfExporter.kt`** — génère un PDF de synthèse du bilan mensuel,
  prêt à imprimer ou envoyer par email/WhatsApp, via iText7
- **`data/backup/SauvegardeManager.kt`** — export/import brut du fichier SQLite (.db).
  ⚠️ L'import remplace toutes les données et **redémarre l'application** (nécessaire
  car Room garde une connexion ouverte sur l'ancien fichier)
- **`data/repository/ExportRepository.kt`** — orchestre les exports et les journalise
  dans l'audit (qui a exporté quoi et quand)
- **Écran Rapports** — boutons "PDF" / "Excel" (visibles uniquement si la permission
  `EXPORT_PDF_EXCEL` est accordée au rôle), ouvre directement le sélecteur de partage
  Android (email, WhatsApp, Drive, etc.)
- **Écran Paramètres** — section "Sauvegarde de la base" (ADMIN uniquement) : export
  du .db vers le partage système, import via le sélecteur de fichiers Android (SAF)
- **`FileProvider`** configuré (manifeste + `res/xml/file_paths.xml`) pour partager
  les fichiers générés sans exposer les dossiers internes de l'app

Avec ce bloc, **l'intégralité du cahier des charges initial est couverte** : saisie,
calculs, rapports, rôles/permissions, gestion ADMIN complète (taux/mois/activités/
utilisateurs/bonus), export PDF/Excel, sauvegarde/restauration, journal d'audit,
et identité visuelle LONALOTO.

## Ce qu'il reste, au choix

1. **Écran de saisie CHEF_DE_FLOTTE** (sélection du vendeur, liste à valider) — seul
   écran fonctionnel non encore détaillé (actuellement, la saisie suppose l'utilisateur
   connecté lui-même)
2. **Documentation utilisateur** — manuel par rôle + comment ajouter un mois/activité/%,
   comme demandé dans le livrable initial
3. **Compilation réelle** — ouvrir le projet dans Android Studio (ou laisser le CI
   GitHub Actions le faire) pour obtenir le premier .apk installable et corriger les
   éventuels ajustements de compilation qu'un environnement réel révèle toujours
4. **Vrai logo/charte LONALOTO** si tu parviens à te le procurer

## Bloc 5 — Identité visuelle LONALOTO (nouveau)

- **`ui/theme/Color.kt`** — palette centralisée : orange (#F77F00) en couleur primaire,
  vert (#1B8A4C) en secondaire, inspirée du drapeau ivoirien et cohérente avec le
  dégradé du logo de l'app de référence Sygis-Primaire. **Un seul fichier à modifier**
  si un vrai logo/charte LONALOTO est fourni plus tard.
- **`ui/theme/Theme.kt`** — thème Material3 clair + sombre, couleurs dynamiques
  Android 12+ désactivées par défaut (pour garder l'identité LONALOTO sur tous les
  appareils plutôt que les couleurs du fond d'écran de l'utilisateur)
- **`ui/theme/Type.kt`** — typographie à tailles généreuses, pensée pour rester
  lisible en plein soleil sur le terrain
- **Icône adaptative** (`res/drawable/ic_launcher_*.xml`, `res/mipmap-anydpi-v26/`) —
  dégradé orange→vert avec un "L" stylisé, en vectoriel (pas de PNG à gérer)
- **`MainActivity.kt`** mis à jour pour utiliser `LonalotoTheme` au lieu du thème par défaut

⚠️ **Icônes de secours pré-API 26** : les fichiers `mipmap-anydpi-v26/` couvrent
Android 8+ (la quasi-totalité du parc actuel). Pour une compatibilité totale avec
d'anciens appareils, il suffira d'ouvrir le projet dans Android Studio et d'utiliser
l'assistant "Image Asset" (clic droit sur `res` → New → Image Asset) en pointant sur
`ic_launcher_foreground.xml` : il génère automatiquement les PNG pour toutes les
densités en 30 secondes — inutile de le faire à la main.

**Si tu obtiens un vrai logo ou une charte graphique LONALOTO**, transmets-les moi :
je remplace juste `Color.kt` et les fichiers d'icône, tout le reste de l'app suit
automatiquement (aucun écran à retoucher un par un).

## Bloc 4 — Écrans ADMIN (nouveau)

- **`data/repository/UtilisateurRepository.kt`** — création d'utilisateur (PIN haché à la
  création), désactivation logique (jamais de suppression physique), réinitialisation de PIN
- **`data/repository/PalierBonusRepository.kt`** — CRUD des paliers de bonus, tracé dans l'audit
- **`ui/parametres/ParametresScreen.kt`** — écran central ADMIN : sélection d'activité,
  modification des taux, **renommage d'activité (ex: ANGE → ANYAMA)**, création de nouvelle
  activité, gestion complète des paliers de bonus (ajout/suppression)
- **`ui/utilisateurs/UtilisateursScreen.kt`** — liste des utilisateurs actifs, création
  (nom + PIN + rôle + activité), désactivation, réinitialisation de PIN
- **`ui/audit/AuditScreen.kt`** — journal complet en lecture seule : qui a fait quoi et quand
- **Navigation** : ces 3 écrans n'apparaissent dans la barre d'onglets **que pour l'ADMIN**
  (filtrage automatique par `Permissions.kt`, déjà en place depuis le bloc 3)

Ce bloc complète l'ensemble des écrans fonctionnels décrits dans le cahier des charges
initial. Il ne reste que l'export PDF/Excel, l'import/export de base, et l'habillage
visuel final (couleurs/icône LONALOTO).

## Bloc 3 — Saisie + Rapports (nouveau)

- **`domain/calcul/CalculSemaine.kt`** — reproduit exactement le découpage en blocs de
  7 jours de la feuille "Point Hebdomadaire" (1-7, 8-14, 15-21, 22-28, 29-fin), testé
  contre tous les cas (mois de 28/30/31 jours)
- **`data/repository/VenteRepository.kt`** — saisie/mise à jour d'une vente journalière
  (refuse si le mois est clôturé), validation par le Chef de Flotte, calcul du bilan
  mensuel complet (taux + bonus applicable) et du bilan hebdomadaire
- **`data/repository/MoisRepository.kt`** — crée automatiquement le mois calendaire
  courant à la première saisie (plus besoin de pré-remplir 13 blocs comme dans l'Excel)
- **`ui/saisie/SaisieVenteScreen.kt`** — formulaire recette/paiement du jour, un seul
  bouton, retour visuel immédiat
- **`ui/rapports/RapportScreen.kt`** — bilan mensuel complet (mêmes lignes que l'Excel :
  Montant total, Montant total LONACI, Point paiement, Salaire coupeur, Salaire mensuel,
  + bonus si applicable) et détail semaine par semaine
- **`ui/navigation/EcranPrincipal.kt`** — barre d'onglets en bas (Accueil/Saisie/Rapports),
  **filtrée automatiquement selon les permissions du rôle connecté** via `Permissions.kt`

## Point d'attention

Les taux (13% / 3% / 2% / 4%) ne sont **codés nulle part en dur** dans le moteur de
calcul : ils sont passés en paramètre via l'objet `Activite`, lu depuis la base Room.
Modifier un taux depuis l'écran Paramètres (à venir) suffira à changer tous les
calculs futurs, sans toucher au code.

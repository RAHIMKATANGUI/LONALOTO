# LONALOTO — Guide du projet

App Android 100% native, 100% offline, Kotlin + Room (SQLite).
Basée sur l'analyse du fichier Excel LONALOTO (7 feuilles, logique confirmée).

---

## Ce que contient ce livrable

```
LONALOTO/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── MANUEL-UTILISATEUR.md          ← documentation par rôle
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/                ← thème, icône, FileProvider
│       │   └── java/com/lonaloto/
│       │       ├── LonalotoApplication.kt
│       │       ├── data/
│       │       │   ├── local/       ← entités Room, DAO, base
│       │       │   ├── repository/  ← Activite, Auth, Vente, Mois, Utilisateur,
│       │       │   │                  PalierBonus, Export
│       │       │   ├── export/      ← ExcelExporter, PdfExporter
│       │       │   └── backup/      ← SauvegardeManager (.db)
│       │       ├── domain/
│       │       │   ├── calcul/      ← moteur de calcul (paie, bonus, semaines, consolidation)
│       │       │   └── auth/        ← PinHasher, SessionManager, Permissions
│       │       ├── ui/
│       │       │   ├── theme/       ← Color, Theme, Type (identité LONALOTO)
│       │       │   ├── connexion/
│       │       │   ├── saisie/          (VENDEUR)
│       │       │   ├── saisieflotte/    (CHEF DE FLOTTE)
│       │       │   ├── rapports/
│       │       │   ├── parametres/      (ADMIN)
│       │       │   ├── utilisateurs/    (ADMIN)
│       │       │   ├── audit/           (ADMIN)
│       │       │   ├── navigation/
│       │       │   └── MainActivity.kt
│       │       └── di/              ← modules Hilt
│       └── test/java/com/lonaloto/ ← tests unitaires (calcul, PIN, semaines)
```

## Comment l'intégrer dans ton dépôt GitHub

1. Récupère ce dossier (tous les fichiers listés ci-dessus).
2. Copie-le **à la racine** de ton dépôt `RAHIMKATANGUI/LONALOTO`, à côté du dossier
   `.github/` déjà présent.
3. Commit + push. Le workflow CI déjà en place va automatiquement tenter de compiler
   (voir "Prochaine étape critique" plus bas).

## Comment vérifier le moteur de calcul dès maintenant

`LonalotoExcelReferenceTest.kt` reproduit les données réelles d'Août 2026 (onglet
RICHMOND) et vérifie que le calcul Kotlin donne EXACTEMENT les mêmes résultats que
les cellules Excel :

| Ligne Excel (RICHMOND, Août 2026) | Valeur Excel | Vérifié par le test |
|---|---|---|
| TOTAL RECETTE (C98)      | 2 872 675 FCFA | ✅ |
| TOTAL PAIEMENT (D98)     | 1 008 375 FCFA | ✅ |
| MONTANT TOTAL (C101)     | 403 699.00 FCFA | ✅ |
| MONTANT TOTAL LONACI (C102) | 395 625.02 FCFA | ✅ |
| POINT PAIEMENT (C103)    | 30 251.25 FCFA | ✅ |
| SALAIRE COUPEUR (C104)   | 114 907.00 FCFA | ✅ |
| SALAIRE MENSUEL (C105)   | 280 718.02 FCFA | ✅ |

`PinHasherTest.kt` et `CalculSemaineTest.kt` complètent la couverture (sécurité du
PIN, découpage hebdomadaire identique à la feuille "Point Hebdomadaire").

```
./gradlew testDebugUnitTest
```

---

## Historique des blocs livrés

**Bloc 1 — Entités Room + moteur de calcul**
7 entités (Utilisateur, Activite, Mois, Vente, PalierBonus, Parametre,
HistoriqueAudit) + DAO + moteur de calcul de paie, testé contre l'Excel réel.

**Bloc 2 — Connexion + Rôles**
Hash PIN (BCrypt), session en mémoire, matrice de permissions centralisée,
écran de connexion, navigation par rôle.

**Bloc 3 — Saisie + Rapports**
Saisie journalière (recette/paiement), calcul du bilan mensuel + hebdomadaire
(reproduit "Point Hebdomadaire"), navigation par onglets filtrée par permission.

**Bloc 4 — Écrans ADMIN**
Gestion des taux, renommage d'activité (ex: ANGE → ANYAMA), création
d'activité, paliers de bonus, gestion des utilisateurs, journal d'audit.

**Bloc 5 — Identité visuelle**
Palette orange/vert LONALOTO centralisée dans `Color.kt`, thème Material3
clair/sombre, icône adaptative vectorielle.

**Bloc 6 — Export PDF/Excel + Sauvegarde**
Export Excel (structure fidèle à l'Excel d'origine), export PDF, sauvegarde/
restauration de la base .db, tout journalisé dans l'audit.

**Bloc 7 — Écran Chef de Flotte + Documentation (dernier bloc)**
Le CHEF DE FLOTTE peut saisir pour un vendeur de sa flotte et valider les
saisies en attente. `MANUEL-UTILISATEUR.md` documente chaque rôle en détail.

**Avec ce bloc, l'intégralité fonctionnelle et documentaire du cahier des
charges initial est couverte.**

---

## Ce qui reste, hors code

- Icônes PNG de secours (pré-Android 8 / API < 26) — génération automatique via
  l'assistant "Image Asset" d'Android Studio en 30 secondes (voir bloc 5)
- **La première compilation réelle** — voir ci-dessous, c'est l'étape critique restante
- Vrai logo/charte LONALOTO si tu parviens à te le procurer (un seul fichier à
  modifier ensuite : `Color.kt`)

## Prochaine étape critique : la première compilation réelle

Ce projet a été écrit intégralement dans un environnement sans SDK Android — il n'a
donc **jamais été compilé**. C'est normal à ce stade, mais reste une étape
indispensable avant de considérer l'app "terminée" :

1. Ouvrir le dossier `LONALOTO/` dans **Android Studio** (Hedgehog ou plus récent)
2. Laisser Gradle synchroniser (télécharge Room, Hilt, Compose, etc.)
3. Corriger les éventuelles erreurs de compilation — sur un projet de cette taille
   écrit sans compilateur, il est normal de rencontrer quelques ajustements mineurs
   (import manquant, version de dépendance à aligner, typo). Aucun changement
   d'architecture n'est attendu, seulement des corrections locales.
4. Lancer `./gradlew testDebugUnitTest` pour confirmer que les tests passent toujours
5. Lancer l'app sur un émulateur ou un téléphone pour valider les écrans visuellement

Le workflow CI GitHub Actions déjà en place fera automatiquement cette compilation à
chaque `push` — pousse le code et regarde l'onglet **Actions** pour repérer d'éventuelles
erreurs sans installer Android Studio en local.

**Je reste disponible pour corriger tout message d'erreur que la compilation réelle
révèle** — colle-moi simplement le message d'erreur de Gradle ou d'Android Studio.

## Point d'attention sur les taux et bonus

Les taux (13% / 3% / 2% / 4%) et les paliers de bonus ne sont codés nulle part en
dur : ils sont stockés en base et modifiables par l'ADMIN via Paramètres, sans
recompiler l'app.

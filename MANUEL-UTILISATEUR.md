# LONALOTO — Manuel d'utilisation

Application de gestion des ventes, commissions et bilans LONALOTO — 100% hors ligne.

---

## Connexion

Au démarrage de l'app :
1. Saisir votre **nom d'utilisateur**
2. Saisir votre **code PIN** (4 à 8 chiffres)
3. Appuyer sur **SE CONNECTER**

Selon votre rôle (ADMIN, CHEF DE FLOTTE ou VENDEUR), les onglets disponibles en bas de l'écran changent automatiquement.

⚠️ Si l'application passe en arrière-plan (vous ouvrez une autre app), vous êtes automatiquement déconnecté — reconnectez-vous en revenant sur LONALOTO. C'est une protection si plusieurs personnes partagent le même téléphone.

---

## Manuel VENDEUR

En tant que vendeur, vous avez accès à 2 onglets : **Saisie** et **Rapports**.

### Saisir ma vente du jour
1. Aller sur l'onglet **Saisie**
2. Entrer la **recette journalière** (montant total encaissé, en FCFA)
3. Entrer le **paiement journalier** (montant total payé aux gagnants, en FCFA)
4. Appuyer sur **ENREGISTRER**

Vous pouvez ressaisir plusieurs fois dans la journée : la dernière saisie remplace la précédente pour ce jour-là (tant que le mois n'est pas clôturé par l'ADMIN).

### Consulter mon bilan
Aller sur l'onglet **Rapports** : vous voyez le calcul complet de votre paie du mois (montant total, salaire coupeur, salaire mensuel), et le détail semaine par semaine.

---

## Manuel CHEF DE FLOTTE

En plus de Saisie et Rapports (pour votre propre activité), vous avez accès à l'onglet **Ma flotte**.

### Saisir pour un vendeur de ma flotte
Utile si un vendeur n'a pas de téléphone ou n'a pas encore saisi lui-même.
1. Aller sur l'onglet **Ma flotte**
2. Choisir le vendeur dans la liste déroulante
3. Entrer la recette et le paiement du jour
4. Appuyer sur **ENREGISTRER POUR CE VENDEUR**

### Valider les saisies de mes vendeurs
Toujours sur l'onglet **Ma flotte**, la section "En attente de validation" liste toutes les saisies non encore validées. Appuyer sur **Valider** à côté de chaque ligne pour la confirmer.

---

## Manuel ADMIN

L'ADMIN a accès à tous les onglets : Accueil, Saisie, Ma flotte (si rattaché à une activité), Rapports, **Paramètres**, **Utilisateurs**, **Audit**.

### Modifier un pourcentage (taux)
1. Aller sur **Paramètres**
2. Sélectionner l'activité concernée (onglets en haut de l'écran)
3. Appuyer sur **Modifier les taux**
4. Changer la valeur souhaitée (ex: `0.13` pour 13%)
5. Appuyer sur **Enregistrer**

Le changement s'applique immédiatement à tous les calculs futurs pour cette activité. Les calculs déjà effectués dans le passé ne sont pas recalculés rétroactivement.

### Renommer une activité (ex: ANGE → ANYAMA)
1. Aller sur **Paramètres**
2. Sélectionner l'activité à renommer
3. Appuyer sur l'icône crayon à côté du nom
4. Entrer le nouveau nom, puis **Renommer**

Tout l'historique (ventes, bilans passés) reste intact et s'affiche automatiquement sous le nouveau nom.

### Créer une nouvelle activité
1. Aller sur **Paramètres**
2. Appuyer sur le bouton **+** en haut à droite
3. Entrer le nom et les 4 taux de départ
4. Appuyer sur **Créer**

### Ajouter un mois
Les mois se créent automatiquement dès qu'une première vente y est saisie — aucune action manuelle n'est nécessaire en temps normal. Si besoin de préparer un mois à l'avance, cela peut être fait depuis le code (fonctionnalité d'interface dédiée à ajouter si le besoin se confirme).

### Configurer un palier de bonus
1. Aller sur **Paramètres**, sélectionner l'activité
2. Dans la section "Paliers de bonus", appuyer sur **+**
3. Entrer le seuil minimum, le seuil maximum (laisser vide si aucun plafond), et le taux de bonus (ex: `0.01` pour 1%)
4. Appuyer sur **Ajouter**

Le bonus s'applique automatiquement dès que la recette mensuelle d'une activité atteint le seuil configuré.

### Gérer les utilisateurs
1. Aller sur **Utilisateurs**
2. Appuyer sur **+** pour créer un compte : nom, code PIN, rôle, activité (sauf pour un ADMIN)
3. Pour désactiver un compte : appuyer sur l'icône corbeille (le compte est désactivé, jamais supprimé — l'historique reste consultable)
4. Pour réinitialiser un PIN oublié : appuyer sur l'icône cadenas

### Consulter le journal d'audit
Aller sur **Audit** : liste complète de toutes les actions effectuées dans l'app (création, modification, suppression, validation, connexion, export), avec qui l'a fait et quand.

### Exporter un rapport (PDF ou Excel)
Depuis l'onglet **Rapports**, appuyer sur **PDF** ou **Excel** en haut de l'écran. Le fichier généré s'ouvre dans le sélecteur de partage standard d'Android (email, WhatsApp, Drive, etc.).

### Sauvegarder ou restaurer la base de données
1. Aller sur **Paramètres**, faire défiler jusqu'à "Sauvegarde de la base"
2. **Exporter (.db)** : crée une copie de toute la base et ouvre le partage système pour l'envoyer où vous voulez (email, clé USB via un gestionnaire de fichiers, etc.)
3. **Importer** : sélectionner un fichier .db précédemment exporté — ⚠️ **remplace toutes les données actuelles** et redémarre l'application automatiquement

---

## Questions fréquentes

**J'ai oublié mon code PIN.** Demandez à l'ADMIN de le réinitialiser depuis l'onglet Utilisateurs.

**Un mois n'apparaît pas dans mes rapports.** Les mois se créent automatiquement à la première saisie. Si vous n'avez encore rien saisi ce mois-ci, c'est normal qu'il n'y ait pas encore de données.

**Je me suis trompé dans une saisie du jour.** Ressaisissez simplement les bons montants pour la même date — la nouvelle saisie remplace l'ancienne (sauf si le mois est déjà clôturé).

**L'app a redémarré après un import de sauvegarde, c'est normal ?** Oui, c'est le comportement attendu : le redémarrage garantit que toutes les données restaurées sont correctement prises en compte.

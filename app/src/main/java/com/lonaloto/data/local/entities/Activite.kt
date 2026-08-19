package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Une activité / flotte / point de vente (ex: "RICHMOND", "ANGE").
 * Remplace le clonage manuel d'onglets Excel : l'ADMIN peut en créer
 * autant qu'il veut, chacune avec ses propres taux.
 *
 * Valeurs initiales identiques à celles trouvées dans le fichier Excel LONALOTO :
 * tauxRecette=0.13, tauxPaiement=0.03, tauxLonaci=0.02, tauxSalaireCoupeur=0.04
 *
 * IMPORTANT : le nom (`nom`) est modifiable à tout moment par l'ADMIN
 * (ex: "ANGE" → "ANYAMA"). Toutes les données liées (Ventes, Mois, Historique)
 * référencent l'activité par son `id`, jamais par son nom — un renommage
 * n'affecte donc AUCUNE donnée historique ni aucun calcul déjà effectué.
 * Seul l'affichage change.
 */
@Entity(
    tableName = "activites",
    indices = [Index(value = ["nom"], unique = true)]
)
data class Activite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nom: String,

    /** Taux Montant Total / Recette — ex: 0.13 pour 13% */
    val tauxRecette: Double,

    /** Taux Montant Total / Paiement — ex: 0.03 pour 3% */
    val tauxPaiement: Double,

    /** Taux LONACI — ex: 0.02 pour 2% */
    val tauxLonaci: Double,

    /** Taux Salaire Coupeur — ex: 0.04 pour 4% */
    val tauxSalaireCoupeur: Double,

    val actif: Boolean = true,

    val dateCreation: Date = Date()
)

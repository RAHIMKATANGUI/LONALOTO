package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Palier de bonus, entièrement configurable par l'ADMIN.
 * La table démarre VIDE : aucun palier n'existe tant que l'ADMIN n'en crée pas.
 * Absent du fichier Excel d'origine — fonctionnalité ajoutée à la demande du client,
 * conçue pour rester 100% modifiable (pas de seuils codés en dur).
 *
 * Le palier s'applique si : seuilMin <= recette_totale_du_mois < seuilMax (ou infini si null).
 */
@Entity(
    tableName = "paliers_bonus",
    foreignKeys = [
        ForeignKey(entity = Activite::class, parentColumns = ["id"], childColumns = ["activiteId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PalierBonus(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val activiteId: Long,

    /** Seuil minimum de recette mensuelle (FCFA) pour déclencher ce palier */
    val seuilMin: Double,

    /** Seuil maximum (FCFA), null = pas de plafond */
    val seuilMax: Double? = null,

    /** Taux de bonus appliqué à la recette totale, ex: 0.01 pour +1% */
    val tauxBonus: Double,

    val actif: Boolean = true
)

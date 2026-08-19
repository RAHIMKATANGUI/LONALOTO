package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Un mois de gestion (remplace les 13 blocs codés en dur dans l'Excel).
 * Créé à la demande par l'ADMIN ("Ajouter un mois") : aucune limite de nombre.
 *
 * cloture = true : verrouille la saisie de ventes pour ce mois (fin de mois validée).
 */
@Entity(
    tableName = "mois",
    indices = [Index(value = ["annee", "numeroMois"], unique = true)]
)
data class Mois(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val annee: Int,

    /** 1 = janvier ... 12 = décembre */
    val numeroMois: Int,

    /** Libellé affiché, ex: "AOÛT 2026" */
    val libelle: String,

    val cloture: Boolean = false,

    val dateCreation: Date = Date()
)

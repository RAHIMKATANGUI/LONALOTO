package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Une ligne de saisie journalière (remplace une ligne "JOUR / DATE / RECETTE / PAIEMENT"
 * d'un onglet Excel). Une seule ligne par (activité, mois, vendeur, date).
 *
 * Le circuit de validation (validePar / dateValidation) permet au CHEF_DE_FLOTTE
 * de valider les saisies de ses vendeurs, comme demandé dans le cahier des charges.
 */
@Entity(
    tableName = "ventes",
    indices = [Index(value = ["activiteId", "moisId", "vendeurId", "date"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = Activite::class, parentColumns = ["id"], childColumns = ["activiteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Mois::class, parentColumns = ["id"], childColumns = ["moisId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Utilisateur::class, parentColumns = ["id"], childColumns = ["vendeurId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Vente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val activiteId: Long,
    val moisId: Long,
    val vendeurId: Long,

    val date: Date,

    /** Recette journalière en FCFA — colonne C de l'Excel */
    val recette: Double,

    /** Paiement journalier en FCFA — colonne D de l'Excel */
    val paiement: Double,

    val saisiParId: Long,

    val validePar: Long? = null,
    val dateValidation: Date? = null,

    val dateSaisie: Date = Date()
)

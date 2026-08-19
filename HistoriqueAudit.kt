package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TypeAction {
    CREATION,
    MODIFICATION,
    SUPPRESSION,
    VALIDATION,
    CONNEXION,
    EXPORT,
    IMPORT
}

/**
 * Journal d'audit : trace qui a modifié quoi et quand.
 * Consultable uniquement par l'ADMIN.
 * ancienneValeur/nouvelleValeur stockées en JSON brut pour rester génériques
 * quelle que soit la table concernée.
 */
@Entity(tableName = "historique_audit")
data class HistoriqueAudit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val utilisateurId: Long,

    val action: TypeAction,

    /** Nom de la table concernée, ex: "ventes", "activites" */
    val tableCible: String,

    val enregistrementId: Long? = null,

    val ancienneValeurJson: String? = null,

    val nouvelleValeurJson: String? = null,

    val dateAction: Date = Date()
)

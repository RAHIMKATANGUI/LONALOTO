package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

enum class Role {
    ADMIN,
    CHEF_DE_FLOTTE,
    VENDEUR
}

/**
 * Un utilisateur de l'application.
 * Authentification par Nom + Code PIN (le PIN n'est JAMAIS stocké en clair,
 * uniquement son hash — voir domain/auth/PinHasher.kt).
 *
 * - ADMIN : activiteId = null (accès transverse à toutes les activités)
 * - CHEF_DE_FLOTTE / VENDEUR : activiteId obligatoire (rattaché à une seule activité/flotte)
 */
@Entity(
    tableName = "utilisateurs",
    foreignKeys = [
        ForeignKey(
            entity = Activite::class,
            parentColumns = ["id"],
            childColumns = ["activiteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Utilisateur(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nom: String,

    /** Hash du PIN (jamais la valeur en clair). Voir PinHasher.hash(pin). */
    val pinHash: String,

    val role: Role,

    val activiteId: Long? = null,

    val actif: Boolean = true,

    val dateCreation: Date = Date()
)

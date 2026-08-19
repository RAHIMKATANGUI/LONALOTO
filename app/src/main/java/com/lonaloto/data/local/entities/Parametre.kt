package com.lonaloto.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Réglages globaux clé/valeur, non liés à une activité précise
 * (ex: durée de session avant re-saisie du PIN, nom de l'organisation, etc.)
 */
@Entity(tableName = "parametres")
data class Parametre(
    @PrimaryKey
    val cle: String,

    val valeur: String
)

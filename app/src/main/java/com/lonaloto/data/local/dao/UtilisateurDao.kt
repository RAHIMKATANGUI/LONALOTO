package com.lonaloto.data.local.dao

import androidx.room.*
import com.lonaloto.data.local.entities.Role
import com.lonaloto.data.local.entities.Utilisateur
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilisateurDao {

    @Insert
    suspend fun inserer(utilisateur: Utilisateur): Long

    @Update
    suspend fun modifier(utilisateur: Utilisateur)

    /** Désactivation logique — jamais de suppression physique (traçabilité). */
    @Query("UPDATE utilisateurs SET actif = 0 WHERE id = :id")
    suspend fun desactiver(id: Long)

    @Query("SELECT * FROM utilisateurs WHERE nom = :nom AND actif = 1 LIMIT 1")
    suspend fun parNom(nom: String): Utilisateur?

    @Query("SELECT * FROM utilisateurs WHERE id = :id")
    suspend fun parId(id: Long): Utilisateur?

    @Query("SELECT * FROM utilisateurs WHERE actif = 1 ORDER BY nom")
    fun tousActifs(): Flow<List<Utilisateur>>

    @Query("SELECT * FROM utilisateurs WHERE activiteId = :activiteId AND actif = 1 ORDER BY nom")
    fun parActivite(activiteId: Long): Flow<List<Utilisateur>>

    @Query("SELECT * FROM utilisateurs WHERE role = :role AND actif = 1")
    fun parRole(role: Role): Flow<List<Utilisateur>>
}

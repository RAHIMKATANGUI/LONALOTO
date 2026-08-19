package com.lonaloto.data.local.dao

import androidx.room.*
import com.lonaloto.data.local.entities.Mois
import kotlinx.coroutines.flow.Flow

@Dao
interface MoisDao {

    @Insert
    suspend fun inserer(mois: Mois): Long

    @Query("UPDATE mois SET cloture = 1 WHERE id = :id")
    suspend fun cloturer(id: Long)

    @Query("UPDATE mois SET cloture = 0 WHERE id = :id")
    suspend fun rouvrir(id: Long)

    @Query("SELECT * FROM mois WHERE id = :id")
    suspend fun parId(id: Long): Mois?

    @Query("SELECT * FROM mois WHERE annee = :annee AND numeroMois = :numeroMois LIMIT 1")
    suspend fun parAnneeMois(annee: Int, numeroMois: Int): Mois?

    @Query("SELECT * FROM mois ORDER BY annee DESC, numeroMois DESC")
    fun tous(): Flow<List<Mois>>
}

package com.lonaloto.data.local.dao

import androidx.room.*
import com.lonaloto.data.local.entities.PalierBonus
import kotlinx.coroutines.flow.Flow

@Dao
interface PalierBonusDao {

    @Insert
    suspend fun inserer(palier: PalierBonus): Long

    @Update
    suspend fun modifier(palier: PalierBonus)

    @Delete
    suspend fun supprimer(palier: PalierBonus)

    @Query("SELECT * FROM paliers_bonus WHERE activiteId = :activiteId AND actif = 1 ORDER BY seuilMin")
    suspend fun parActivite(activiteId: Long): List<PalierBonus>

    @Query("SELECT * FROM paliers_bonus WHERE activiteId = :activiteId ORDER BY seuilMin")
    fun tousParActivite(activiteId: Long): Flow<List<PalierBonus>>
}

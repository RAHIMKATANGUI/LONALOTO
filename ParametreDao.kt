package com.lonaloto.data.local.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.lonaloto.data.local.entities.Parametre

@Dao
interface ParametreDao {

    @Upsert
    suspend fun definir(parametre: Parametre)

    @Query("SELECT * FROM parametres WHERE cle = :cle LIMIT 1")
    suspend fun obtenir(cle: String): Parametre?

    @Query("SELECT * FROM parametres")
    suspend fun tous(): List<Parametre>
}

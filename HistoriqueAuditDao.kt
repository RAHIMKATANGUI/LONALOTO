package com.lonaloto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lonaloto.data.local.entities.HistoriqueAudit
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoriqueAuditDao {

    @Insert
    suspend fun inserer(entree: HistoriqueAudit): Long

    /** Journal complet, le plus récent en premier — consultable uniquement par l'ADMIN. */
    @Query("SELECT * FROM historique_audit ORDER BY dateAction DESC")
    fun tout(): Flow<List<HistoriqueAudit>>

    @Query("SELECT * FROM historique_audit WHERE utilisateurId = :utilisateurId ORDER BY dateAction DESC")
    fun parUtilisateur(utilisateurId: Long): Flow<List<HistoriqueAudit>>

    @Query("SELECT * FROM historique_audit WHERE tableCible = :table ORDER BY dateAction DESC")
    fun parTable(table: String): Flow<List<HistoriqueAudit>>
}

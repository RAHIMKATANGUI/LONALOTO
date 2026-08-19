package com.lonaloto.data.local.dao

import androidx.room.*
import com.lonaloto.data.local.entities.Activite
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiviteDao {

    @Insert
    suspend fun inserer(activite: Activite): Long

    /** Utilisé par l'ADMIN pour modifier les taux (%) et/ou le nom. */
    @Update
    suspend fun modifier(activite: Activite)

    /**
     * Renommage seul, sans toucher aux taux — ex: "ANGE" devient "ANYAMA".
     * L'id ne change pas, donc tout l'historique (ventes, bilans, audit) reste intact.
     */
    @Query("UPDATE activites SET nom = :nouveauNom WHERE id = :id")
    suspend fun renommer(id: Long, nouveauNom: String)

    @Query("UPDATE activites SET actif = 0 WHERE id = :id")
    suspend fun desactiver(id: Long)

    @Query("SELECT * FROM activites WHERE id = :id")
    suspend fun parId(id: Long): Activite?

    /** Utilisé pour vérifier l'unicité du nom avant renommage (hors l'activité elle-même). */
    @Query("SELECT * FROM activites WHERE nom = :nom AND id != :idExclu LIMIT 1")
    suspend fun autreActiviteAvecCeNom(nom: String, idExclu: Long): Activite?

    @Query("SELECT * FROM activites WHERE actif = 1 ORDER BY nom")
    fun toutesActives(): Flow<List<Activite>>
}

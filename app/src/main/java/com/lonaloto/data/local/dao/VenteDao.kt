package com.lonaloto.data.local.dao

import androidx.room.*
import com.lonaloto.data.local.entities.Vente
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Agrégat mensuel brut (avant application des taux) pour une activité donnée.
 * Reproduit "TOTAL DU MOIS" (ligne 55/98/... de l'Excel) : SUM(recette), SUM(paiement).
 */
data class TotauxMensuels(
    val totalRecette: Double,
    val totalPaiement: Double
)

@Dao
interface VenteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserer(vente: Vente): Long

    @Update
    suspend fun modifier(vente: Vente)

    @Delete
    suspend fun supprimer(vente: Vente)

    @Query("UPDATE ventes SET validePar = :validateurId, dateValidation = :date WHERE id = :venteId")
    suspend fun valider(venteId: Long, validateurId: Long, date: Date = Date())

    @Query("SELECT * FROM ventes WHERE activiteId = :activiteId AND moisId = :moisId ORDER BY date")
    fun parActiviteEtMois(activiteId: Long, moisId: Long): Flow<List<Vente>>

    @Query("SELECT * FROM ventes WHERE vendeurId = :vendeurId AND moisId = :moisId ORDER BY date")
    fun parVendeurEtMois(vendeurId: Long, moisId: Long): Flow<List<Vente>>

    @Query("SELECT * FROM ventes WHERE vendeurId = :vendeurId AND date = :date LIMIT 1")
    suspend fun saisieDuJour(vendeurId: Long, date: Date): Vente?

    /** Saisies non encore validées par le Chef de Flotte, les plus récentes en premier. */
    @Query(
        """
        SELECT * FROM ventes
        WHERE activiteId = :activiteId AND moisId = :moisId AND validePar IS NULL
        ORDER BY date DESC
        """
    )
    fun nonValideesParActiviteEtMois(activiteId: Long, moisId: Long): Flow<List<Vente>>

    /**
     * Reproduit "TOTAL DU MOIS" de l'Excel : SUM(recette), SUM(paiement)
     * pour une activité et un mois donnés, toutes saisies confondues (tous vendeurs).
     */
    @Query(
        """
        SELECT COALESCE(SUM(recette), 0) AS totalRecette,
               COALESCE(SUM(paiement), 0) AS totalPaiement
        FROM ventes
        WHERE activiteId = :activiteId AND moisId = :moisId
        """
    )
    suspend fun totauxMensuels(activiteId: Long, moisId: Long): TotauxMensuels

    /** Même agrégat, mais restreint à un seul vendeur (bilan personnel). */
    @Query(
        """
        SELECT COALESCE(SUM(recette), 0) AS totalRecette,
               COALESCE(SUM(paiement), 0) AS totalPaiement
        FROM ventes
        WHERE vendeurId = :vendeurId AND moisId = :moisId
        """
    )
    suspend fun totauxMensuelsVendeur(vendeurId: Long, moisId: Long): TotauxMensuels

    /** Totaux d'une semaine calendaire (1-7, 8-14, ...), comme "Point Hebdomadaire". */
    @Query(
        """
        SELECT COALESCE(SUM(recette), 0) AS totalRecette,
               COALESCE(SUM(paiement), 0) AS totalPaiement
        FROM ventes
        WHERE activiteId = :activiteId AND moisId = :moisId
          AND date BETWEEN :debut AND :fin
        """
    )
    suspend fun totauxSemaine(activiteId: Long, moisId: Long, debut: Date, fin: Date): TotauxMensuels
}

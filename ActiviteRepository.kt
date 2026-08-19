package com.lonaloto.data.repository

import com.lonaloto.data.local.dao.ActiviteDao
import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.entities.Activite
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.TypeAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ResultatRenommage {
    data class Succes(val ancienNom: String, val nouveauNom: String) : ResultatRenommage()
    data class NomDejaUtilise(val nomConflictuel: String) : ResultatRenommage()
    data object ActiviteIntrouvable : ResultatRenommage()
}

/**
 * Point d'entrée unique pour manipuler les Activités.
 * Le renommage (ex: "ANGE" → "ANYAMA") ne modifie QUE le libellé affiché :
 * l'id reste identique, donc toutes les ventes et bilans déjà enregistrés
 * pour cette activité restent valides et continuent de s'afficher sous le
 * nouveau nom automatiquement (ils sont liés par activiteId, pas par nom).
 */
@Singleton
class ActiviteRepository @Inject constructor(
    private val activiteDao: ActiviteDao,
    private val historiqueAuditDao: HistoriqueAuditDao
) {

    fun toutesActives(): Flow<List<Activite>> = activiteDao.toutesActives()

    suspend fun parId(id: Long): Activite? = activiteDao.parId(id)

    suspend fun creer(activite: Activite, auteurId: Long): Long {
        val id = activiteDao.inserer(activite)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.CREATION,
                tableCible = "activites",
                enregistrementId = id,
                nouvelleValeurJson = """{"nom":"${activite.nom}"}"""
            )
        )
        return id
    }

    /**
     * Renomme une activité existante (ex: ANGE → ANYAMA).
     * - Vérifie qu'aucune autre activité n'a déjà ce nom (contrainte d'unicité).
     * - Journalise l'ancien et le nouveau nom dans l'historique d'audit.
     * - Ne touche à aucune autre donnée (ventes, mois, bilans) : elles sont
     *   liées par id et continueront de s'afficher correctement sous le nouveau nom.
     */
    suspend fun renommer(activiteId: Long, nouveauNom: String, auteurId: Long): ResultatRenommage {
        val nomNettoye = nouveauNom.trim()

        val activiteExistante = activiteDao.parId(activiteId)
            ?: return ResultatRenommage.ActiviteIntrouvable

        val conflit = activiteDao.autreActiviteAvecCeNom(nomNettoye, idExclu = activiteId)
        if (conflit != null) {
            return ResultatRenommage.NomDejaUtilise(nomConflictuel = nomNettoye)
        }

        val ancienNom = activiteExistante.nom
        activiteDao.renommer(activiteId, nomNettoye)

        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.MODIFICATION,
                tableCible = "activites",
                enregistrementId = activiteId,
                ancienneValeurJson = """{"nom":"$ancienNom"}""",
                nouvelleValeurJson = """{"nom":"$nomNettoye"}"""
            )
        )

        return ResultatRenommage.Succes(ancienNom = ancienNom, nouveauNom = nomNettoye)
    }

    suspend fun modifierTaux(activite: Activite, auteurId: Long) {
        val avant = activiteDao.parId(activite.id)
        activiteDao.modifier(activite)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.MODIFICATION,
                tableCible = "activites",
                enregistrementId = activite.id,
                ancienneValeurJson = avant?.let {
                    """{"tauxRecette":${it.tauxRecette},"tauxPaiement":${it.tauxPaiement},"tauxLonaci":${it.tauxLonaci},"tauxSalaireCoupeur":${it.tauxSalaireCoupeur}}"""
                },
                nouvelleValeurJson = """{"tauxRecette":${activite.tauxRecette},"tauxPaiement":${activite.tauxPaiement},"tauxLonaci":${activite.tauxLonaci},"tauxSalaireCoupeur":${activite.tauxSalaireCoupeur}}"""
            )
        )
    }
}

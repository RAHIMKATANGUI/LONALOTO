package com.lonaloto.data.repository

import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.dao.PalierBonusDao
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.PalierBonus
import com.lonaloto.data.local.entities.TypeAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère les paliers de bonus — table vide par défaut, entièrement pilotée par l'ADMIN
 * (voir Étape 3 de l'architecture : aucun seuil codé en dur).
 */
@Singleton
class PalierBonusRepository @Inject constructor(
    private val palierBonusDao: PalierBonusDao,
    private val historiqueAuditDao: HistoriqueAuditDao
) {

    fun parActivite(activiteId: Long): Flow<List<PalierBonus>> = palierBonusDao.tousParActivite(activiteId)

    suspend fun creer(palier: PalierBonus, auteurId: Long): Long {
        val id = palierBonusDao.inserer(palier)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.CREATION,
                tableCible = "paliers_bonus",
                enregistrementId = id,
                nouvelleValeurJson = """{"seuilMin":${palier.seuilMin},"seuilMax":${palier.seuilMax},"tauxBonus":${palier.tauxBonus}}"""
            )
        )
        return id
    }

    suspend fun modifier(palier: PalierBonus, auteurId: Long) {
        palierBonusDao.modifier(palier)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.MODIFICATION,
                tableCible = "paliers_bonus",
                enregistrementId = palier.id,
                nouvelleValeurJson = """{"seuilMin":${palier.seuilMin},"seuilMax":${palier.seuilMax},"tauxBonus":${palier.tauxBonus},"actif":${palier.actif}}"""
            )
        )
    }

    suspend fun supprimer(palier: PalierBonus, auteurId: Long) {
        palierBonusDao.supprimer(palier)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.SUPPRESSION,
                tableCible = "paliers_bonus",
                enregistrementId = palier.id
            )
        )
    }
}

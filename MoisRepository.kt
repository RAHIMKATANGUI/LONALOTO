package com.lonaloto.data.repository

import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.dao.MoisDao
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.Mois
import com.lonaloto.data.local.entities.TypeAction
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private val NOMS_MOIS_FR = listOf(
    "JANVIER", "FÉVRIER", "MARS", "AVRIL", "MAI", "JUIN",
    "JUILLET", "AOÛT", "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DÉCEMBRE"
)

@Singleton
class MoisRepository @Inject constructor(
    private val moisDao: MoisDao,
    private val historiqueAuditDao: HistoriqueAuditDao
) {

    fun tousLesMois(): Flow<List<Mois>> = moisDao.tous()

    /**
     * Retourne le Mois correspondant au mois calendaire actuel, en le créant
     * automatiquement s'il n'existe pas encore — remplace la nécessité de
     * pré-remplir 13 blocs comme dans l'Excel : un mois apparaît dès qu'on
     * y saisit une première vente.
     */
    suspend fun moisCourant(auteurId: Long): Mois {
        val cal = Calendar.getInstance()
        val annee = cal.get(Calendar.YEAR)
        val numeroMois = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH est 0-indexé

        return obtenirOuCreer(annee, numeroMois, auteurId)
    }

    suspend fun obtenirOuCreer(annee: Int, numeroMois: Int, auteurId: Long): Mois {
        moisDao.parAnneeMois(annee, numeroMois)?.let { return it }

        val libelle = "${NOMS_MOIS_FR[numeroMois - 1]} $annee"
        val nouveauMois = Mois(annee = annee, numeroMois = numeroMois, libelle = libelle)
        val id = moisDao.inserer(nouveauMois)

        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.CREATION,
                tableCible = "mois",
                enregistrementId = id,
                nouvelleValeurJson = """{"libelle":"$libelle"}"""
            )
        )

        return nouveauMois.copy(id = id)
    }

    /** Clôture un mois — verrouille toute nouvelle saisie (ADMIN uniquement, vérifié côté UI). */
    suspend fun cloturer(moisId: Long, auteurId: Long) {
        moisDao.cloturer(moisId)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.MODIFICATION,
                tableCible = "mois",
                enregistrementId = moisId,
                nouvelleValeurJson = """{"cloture":true}"""
            )
        )
    }

    suspend fun rouvrir(moisId: Long, auteurId: Long) {
        moisDao.rouvrir(moisId)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.MODIFICATION,
                tableCible = "mois",
                enregistrementId = moisId,
                nouvelleValeurJson = """{"cloture":false}"""
            )
        )
    }
}

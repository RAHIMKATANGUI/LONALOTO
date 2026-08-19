package com.lonaloto.data.repository

import com.lonaloto.data.local.dao.*
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.TypeAction
import com.lonaloto.data.local.entities.Vente
import com.lonaloto.domain.calcul.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

sealed class ResultatSaisie {
    data class Succes(val venteId: Long) : ResultatSaisie()
    data object MoisCloture : ResultatSaisie()
    data class Erreur(val message: String) : ResultatSaisie()
}

@Singleton
class VenteRepository @Inject constructor(
    private val venteDao: VenteDao,
    private val moisDao: MoisDao,
    private val activiteDao: ActiviteDao,
    private val palierBonusDao: PalierBonusDao,
    private val historiqueAuditDao: HistoriqueAuditDao
) {

    fun ventesDuMois(activiteId: Long, moisId: Long): Flow<List<Vente>> =
        venteDao.parActiviteEtMois(activiteId, moisId)

    fun ventesVendeurDuMois(vendeurId: Long, moisId: Long): Flow<List<Vente>> =
        venteDao.parVendeurEtMois(vendeurId, moisId)

    fun ventesNonValidees(activiteId: Long, moisId: Long): Flow<List<Vente>> =
        venteDao.nonValideesParActiviteEtMois(activiteId, moisId)

    /**
     * Saisit (ou met à jour) la recette/paiement d'un vendeur pour une date donnée.
     * Refuse la saisie si le mois est clôturé — protège les données déjà validées
     * par l'ADMIN en fin de mois.
     */
    suspend fun saisir(
        activiteId: Long,
        moisId: Long,
        vendeurId: Long,
        date: Date,
        recette: Double,
        paiement: Double,
        saisiParId: Long
    ): ResultatSaisie {
        if (recette < 0 || paiement < 0) {
            return ResultatSaisie.Erreur("La recette et le paiement ne peuvent pas être négatifs")
        }

        val mois = moisDao.parId(moisId) ?: return ResultatSaisie.Erreur("Mois introuvable")
        if (mois.cloture) {
            return ResultatSaisie.MoisCloture
        }

        val saisieExistante = venteDao.saisieDuJour(vendeurId, date)

        val id = if (saisieExistante != null) {
            val miseAJour = saisieExistante.copy(
                recette = recette,
                paiement = paiement,
                dateSaisie = Date()
            )
            venteDao.modifier(miseAJour)
            historiqueAuditDao.inserer(
                HistoriqueAudit(
                    utilisateurId = saisiParId,
                    action = TypeAction.MODIFICATION,
                    tableCible = "ventes",
                    enregistrementId = saisieExistante.id,
                    ancienneValeurJson = """{"recette":${saisieExistante.recette},"paiement":${saisieExistante.paiement}}""",
                    nouvelleValeurJson = """{"recette":$recette,"paiement":$paiement}"""
                )
            )
            saisieExistante.id
        } else {
            val nouvelleVente = Vente(
                activiteId = activiteId,
                moisId = moisId,
                vendeurId = vendeurId,
                date = date,
                recette = recette,
                paiement = paiement,
                saisiParId = saisiParId
            )
            val nouvelId = venteDao.inserer(nouvelleVente)
            historiqueAuditDao.inserer(
                HistoriqueAudit(
                    utilisateurId = saisiParId,
                    action = TypeAction.CREATION,
                    tableCible = "ventes",
                    enregistrementId = nouvelId,
                    nouvelleValeurJson = """{"recette":$recette,"paiement":$paiement}"""
                )
            )
            nouvelId
        }

        return ResultatSaisie.Succes(id)
    }

    /** Validation d'une saisie par le CHEF DE FLOTTE (ou l'ADMIN). */
    suspend fun valider(venteId: Long, validateurId: Long) {
        venteDao.valider(venteId, validateurId)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = validateurId,
                action = TypeAction.VALIDATION,
                tableCible = "ventes",
                enregistrementId = venteId
            )
        )
    }

    /**
     * Calcule le bilan mensuel complet d'une activité, en appliquant les taux
     * courants de l'activité et le palier de bonus correspondant (s'il y en a un).
     * Reproduit exactement "CALCUL DE LA PAIE ET DES MARGES DU MOIS" de l'Excel.
     */
    suspend fun bilanMensuel(activiteId: Long, moisId: Long): ResultatPaieMensuelle? {
        val activite = activiteDao.parId(activiteId) ?: return null
        val totaux = venteDao.totauxMensuels(activiteId, moisId)

        val paliers = palierBonusDao.parActivite(activiteId)
        val selection = CalculBonus.determinerPalier(paliers, totaux.totalRecette)

        return CalculPaieMensuelle.calculer(
            activite = activite,
            totalRecette = totaux.totalRecette,
            totalPaiement = totaux.totalPaiement,
            bonusApplicable = selection.palier
        )
    }

    /** Bilan personnel d'un vendeur (sans application des taux — juste ses totaux bruts). */
    suspend fun totauxVendeur(vendeurId: Long, moisId: Long): TotauxMensuels =
        venteDao.totauxMensuelsVendeur(vendeurId, moisId)

    /**
     * Bilan hebdomadaire : découpe le mois en blocs de 7 jours (comme "Point Hebdomadaire")
     * et retourne les totaux recette/paiement de chaque semaine.
     */
    suspend fun bilanHebdomadaire(activiteId: Long, moisId: Long, annee: Int, numeroMoisCalendaire: Int): List<TotauxMensuels> {
        val joursDansMois = joursDansLeMois(annee, numeroMoisCalendaire)
        val nbSemaines = CalculSemaine.nombreSemaines(joursDansMois)

        return (1..nbSemaines).map { numeroSemaine ->
            val bornes = CalculSemaine.bornes(numeroSemaine, joursDansMois)
            val debut = dateDuMois(annee, numeroMoisCalendaire, bornes.first)
            val fin = dateDuMois(annee, numeroMoisCalendaire, bornes.last)
            venteDao.totauxSemaine(activiteId, moisId, debut, fin)
        }
    }

    private fun joursDansLeMois(annee: Int, numeroMoisCalendaire: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(annee, numeroMoisCalendaire - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun dateDuMois(annee: Int, numeroMoisCalendaire: Int, jour: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(annee, numeroMoisCalendaire - 1, jour, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}

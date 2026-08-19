package com.lonaloto.domain.calcul

/**
 * Ligne consolidée pour une activité, dans un mois donné.
 * Reproduit une ligne de la feuille "Synthèse Mensuelle".
 */
data class LigneConsolidation(
    val activiteId: Long,
    val activiteNom: String,
    val resultat: ResultatPaieMensuelle
)

/**
 * Total consolidé toutes activités confondues pour un mois.
 * Reproduit "TOTAL GÉNÉRAL" de la feuille "Synthèse Mensuelle"
 * et une ligne de la feuille "Consolidation Globale".
 */
data class ConsolidationMensuelle(
    val lignes: List<LigneConsolidation>,
    val totalRecette: Double,
    val totalPaiement: Double,
    val totalMontantTotal: Double,
    val totalMontantLonaci: Double,
    val totalPointPaiement: Double,
    val totalSalaireCoupeur: Double,
    val totalSalaireMensuel: Double
)

object CalculConsolidation {

    fun consolider(lignes: List<LigneConsolidation>): ConsolidationMensuelle {
        fun somme(selecteur: (ResultatPaieMensuelle) -> Double) =
            lignes.sumOf { selecteur(it.resultat) }

        return ConsolidationMensuelle(
            lignes = lignes,
            totalRecette = somme { it.totalRecette },
            totalPaiement = somme { it.totalPaiement },
            totalMontantTotal = somme { it.montantTotal },
            totalMontantLonaci = somme { it.montantTotalLonaci },
            totalPointPaiement = somme { it.pointPaiement },
            totalSalaireCoupeur = somme { it.salaireCoupeur },
            totalSalaireMensuel = somme { it.salaireMensuelFinal }
        )
    }

    /**
     * Cumul annuel (colonne "CUMUL SALAIRE MENSUEL" de la feuille "Consolidation Globale") :
     * somme progressive du salaire mensuel final, mois après mois, dans l'ordre chronologique.
     */
    fun cumulAnnuel(consolidationsParMois: List<ConsolidationMensuelle>): List<Double> {
        var cumul = 0.0
        return consolidationsParMois.map { moisConsolide ->
            cumul += moisConsolide.totalSalaireMensuel
            cumul
        }
    }
}

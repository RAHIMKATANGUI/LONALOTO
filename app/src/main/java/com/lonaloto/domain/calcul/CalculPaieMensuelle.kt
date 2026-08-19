package com.lonaloto.domain.calcul

import com.lonaloto.data.local.entities.Activite

/**
 * Résultat du calcul de paie mensuelle pour une activité donnée.
 * Les noms des champs reprennent volontairement le vocabulaire exact
 * du fichier Excel LONALOTO ("MONTANT TOTAL", "SALAIRE COUPEUR", etc.)
 * pour que la correspondance reste évidente lors des vérifications terrain.
 */
data class ResultatPaieMensuelle(
    val totalRecette: Double,
    val totalPaiement: Double,
    val montantTotal: Double,
    val montantTotalLonaci: Double,
    val pointPaiement: Double,
    val salaireCoupeur: Double,
    /** Salaire mensuel AVANT bonus éventuel — équivalent exact de la cellule Excel. */
    val salaireMensuelBase: Double,
    /** Bonus appliqué (0 si aucun palier ne correspond). Voir CalculBonus.kt */
    val montantBonus: Double,
    /** salaireMensuelBase + montantBonus — valeur finale à verser. */
    val salaireMensuelFinal: Double
)

/**
 * Reproduit EXACTEMENT la section "CALCUL DE LA PAIE ET DES MARGES DU MOIS"
 * de chaque onglet du fichier Excel LONALOTO :
 *
 *   MONTANT TOTAL        = TOTAL RECETTE × tauxRecette + TOTAL PAIEMENT × tauxPaiement
 *   MONTANT TOTAL LONACI = MONTANT TOTAL − (MONTANT TOTAL × tauxLonaci)
 *   POINT PAIEMENT       = TOTAL PAIEMENT × tauxPaiement
 *   SALAIRE COUPEUR      = TOTAL RECETTE × tauxSalaireCoupeur
 *   SALAIRE MENSUEL      = MONTANT TOTAL LONACI − SALAIRE COUPEUR
 *
 * Vérifié contre les données réelles du fichier Excel (Août 2026, activité RICHMOND) :
 *   totalRecette=2 872 675 / totalPaiement=1 008 375
 *   → montantTotal=403 699.0 / montantTotalLonaci=395 625.02
 *   → pointPaiement=30 251.25 / salaireCoupeur=114 907.0 / salaireMensuelBase=280 718.02
 * (voir le test LonalotoExcelReferenceTest pour la vérification automatisée)
 */
object CalculPaieMensuelle {

    fun calculer(
        activite: Activite,
        totalRecette: Double,
        totalPaiement: Double,
        bonusApplicable: PalierBonusApplicable? = null
    ): ResultatPaieMensuelle {
        require(totalRecette >= 0) { "totalRecette ne peut pas être négatif" }
        require(totalPaiement >= 0) { "totalPaiement ne peut pas être négatif" }

        val montantTotal = totalRecette * activite.tauxRecette + totalPaiement * activite.tauxPaiement
        val montantTotalLonaci = montantTotal - (montantTotal * activite.tauxLonaci)
        val pointPaiement = totalPaiement * activite.tauxPaiement
        val salaireCoupeur = totalRecette * activite.tauxSalaireCoupeur
        val salaireMensuelBase = montantTotalLonaci - salaireCoupeur

        val montantBonus = bonusApplicable?.let { totalRecette * it.tauxBonus } ?: 0.0
        val salaireMensuelFinal = salaireMensuelBase + montantBonus

        return ResultatPaieMensuelle(
            totalRecette = totalRecette,
            totalPaiement = totalPaiement,
            montantTotal = arrondiFcfa(montantTotal),
            montantTotalLonaci = arrondiFcfa(montantTotalLonaci),
            pointPaiement = arrondiFcfa(pointPaiement),
            salaireCoupeur = arrondiFcfa(salaireCoupeur),
            salaireMensuelBase = arrondiFcfa(salaireMensuelBase),
            montantBonus = arrondiFcfa(montantBonus),
            salaireMensuelFinal = arrondiFcfa(salaireMensuelFinal)
        )
    }

    /**
     * Arrondi à 2 décimales, comme Excel affiche les FCFA.
     * Utilise "banker's avoidance" standard (HALF_UP) pour rester
     * prévisible et vérifiable manuellement par un utilisateur non technique.
     */
    private fun arrondiFcfa(valeur: Double): Double =
        java.math.BigDecimal(valeur)
            .setScale(2, java.math.RoundingMode.HALF_UP)
            .toDouble()
}

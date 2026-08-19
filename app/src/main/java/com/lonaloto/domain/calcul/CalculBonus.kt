package com.lonaloto.domain.calcul

import com.lonaloto.data.local.entities.PalierBonus

/** Palier retenu pour le calcul, avec traçabilité de l'id d'origine. */
data class PalierBonusApplicable(
    val palierId: Long,
    val seuilMin: Double,
    val seuilMax: Double?,
    val tauxBonus: Double
)

/**
 * Détermine quel palier de bonus s'applique pour une recette mensuelle donnée.
 *
 * Règles :
 * - La table des paliers est vide par défaut (fidèle au fichier Excel d'origine,
 *   qui ne contient aucun bonus) → aucun palier trouvé = aucun bonus, comportement inchangé.
 * - Un palier s'applique si seuilMin <= recette < seuilMax (ou infini si seuilMax == null).
 * - Si plusieurs paliers actifs se chevauchent (erreur de saisie de l'ADMIN),
 *   on retient le taux le plus élevé et on le signale à l'appelant (pour log d'audit).
 */
object CalculBonus {

    data class ResultatSelectionPalier(
        val palier: PalierBonusApplicable?,
        val chevauchementDetecte: Boolean
    )

    fun determinerPalier(paliersActifs: List<PalierBonus>, recetteTotaleMois: Double): ResultatSelectionPalier {
        val correspondants = paliersActifs.filter { palier ->
            palier.actif &&
                recetteTotaleMois >= palier.seuilMin &&
                (palier.seuilMax == null || recetteTotaleMois < palier.seuilMax)
        }

        if (correspondants.isEmpty()) {
            return ResultatSelectionPalier(palier = null, chevauchementDetecte = false)
        }

        val retenu = correspondants.maxBy { it.tauxBonus }
        val chevauchement = correspondants.size > 1

        return ResultatSelectionPalier(
            palier = PalierBonusApplicable(
                palierId = retenu.id,
                seuilMin = retenu.seuilMin,
                seuilMax = retenu.seuilMax,
                tauxBonus = retenu.tauxBonus
            ),
            chevauchementDetecte = chevauchement
        )
    }
}

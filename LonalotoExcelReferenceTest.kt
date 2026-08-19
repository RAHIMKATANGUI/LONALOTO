package com.lonaloto.domain.calcul

import com.lonaloto.data.local.entities.Activite
import com.lonaloto.data.local.entities.PalierBonus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test de non-régression : vérifie que le moteur de calcul Kotlin produit
 * EXACTEMENT les mêmes résultats que le fichier Excel LONALOTO d'origine.
 *
 * Données source : onglet RICHMOND, mois d'AOÛT 2026, lignes 67 à 83
 * (17 jours saisis sur 31, le reste du mois n'ayant pas encore de saisie).
 *
 * Valeurs attendues copiées depuis les cellules calculées du fichier Excel :
 *   C101 (MONTANT TOTAL)        = 403699.0
 *   C102 (MONTANT TOTAL LONACI) = 395625.02
 *   C103 (POINT PAIEMENT)       = 30251.25
 *   C104 (SALAIRE COUPEUR)      = 114907.0
 *   C105 (SALAIRE MENSUEL)      = 280718.02
 */
class LonalotoExcelReferenceTest {

    /** Taux exacts trouvés dans le fichier Excel (cellules D5-D8 de l'onglet RICHMOND). */
    private val activiteRichmond = Activite(
        id = 1,
        nom = "RICHMOND",
        tauxRecette = 0.13,
        tauxPaiement = 0.03,
        tauxLonaci = 0.02,
        tauxSalaireCoupeur = 0.04
    )

    /** Recettes journalières, jours 1 à 17 d'août 2026 (colonne C de l'Excel). */
    private val recettesAout = listOf(
        273600.0, 243150.0, 163700.0, 170425.0, 170075.0, 163225.0, 127100.0,
        140975.0, 4650.0, 208875.0, 179925.0, 163600.0, 187175.0, 212525.0,
        141375.0, 185700.0, 136600.0
    )

    /** Paiements journaliers, jours 1 à 17 d'août 2026 (colonne D de l'Excel). */
    private val paiementsAout = listOf(
        179000.0, 53150.0, 65000.0, 46550.0, 27400.0, 143000.0, 41525.0,
        25500.0, 0.0, 7500.0, 5000.0, 134000.0, 7000.0, 85000.0,
        82000.0, 12750.0, 94000.0
    )

    @Test
    fun `totaux mensuels correspondent au SUM Excel`() {
        val totalRecette = recettesAout.sum()
        val totalPaiement = paiementsAout.sum()

        // Vérifiés manuellement contre l'Excel : C98 et D98
        assertEquals(2_872_675.0, totalRecette, 0.001)
        assertEquals(1_008_375.0, totalPaiement, 0.001)
    }

    @Test
    fun `calcul de paie mensuelle reproduit exactement les cellules Excel`() {
        val totalRecette = recettesAout.sum()
        val totalPaiement = paiementsAout.sum()

        val resultat = CalculPaieMensuelle.calculer(
            activite = activiteRichmond,
            totalRecette = totalRecette,
            totalPaiement = totalPaiement
        )

        assertEquals(403_699.0, resultat.montantTotal, 0.01)
        assertEquals(395_625.02, resultat.montantTotalLonaci, 0.01)
        assertEquals(30_251.25, resultat.pointPaiement, 0.01)
        assertEquals(114_907.0, resultat.salaireCoupeur, 0.01)
        assertEquals(280_718.02, resultat.salaireMensuelBase, 0.01)

        // Aucun bonus configuré par défaut → salaire final = salaire de base
        assertEquals(0.0, resultat.montantBonus, 0.001)
        assertEquals(resultat.salaireMensuelBase, resultat.salaireMensuelFinal, 0.001)
    }

    @Test
    fun `sans aucun palier configure le comportement reste identique a l'Excel d'origine`() {
        val resultatSansPalier = CalculBonus.determinerPalier(
            paliersActifs = emptyList(),
            recetteTotaleMois = recettesAout.sum()
        )

        assertEquals(null, resultatSansPalier.palier)
        assertEquals(false, resultatSansPalier.chevauchementDetecte)
    }

    @Test
    fun `renommer une activite ne change aucun resultat de calcul`() {
        // ANGE devient ANYAMA : seul le nom change, les taux et l'id restent identiques.
        val activiteAvantRenommage = activiteRichmond.copy(id = 7, nom = "ANGE")
        val activiteApresRenommage = activiteAvantRenommage.copy(nom = "ANYAMA")

        val resultatAvant = CalculPaieMensuelle.calculer(
            activite = activiteAvantRenommage,
            totalRecette = recettesAout.sum(),
            totalPaiement = paiementsAout.sum()
        )
        val resultatApres = CalculPaieMensuelle.calculer(
            activite = activiteApresRenommage,
            totalRecette = recettesAout.sum(),
            totalPaiement = paiementsAout.sum()
        )

        assertEquals(resultatAvant.salaireMensuelFinal, resultatApres.salaireMensuelFinal, 0.001)
        assertEquals(resultatAvant.montantTotal, resultatApres.montantTotal, 0.001)
    }

    @Test
    fun `un palier de bonus configure par l'ADMIN est correctement applique`() {
        // Exemple : +1% si la recette mensuelle dépasse 2 500 000 FCFA
        val palier = PalierBonus(
            id = 1,
            activiteId = 1,
            seuilMin = 2_500_000.0,
            seuilMax = null,
            tauxBonus = 0.01,
            actif = true
        )

        val totalRecette = recettesAout.sum() // 2 872 675 → dépasse le seuil
        val selection = CalculBonus.determinerPalier(listOf(palier), totalRecette)

        assertEquals(1L, selection.palier?.palierId)

        val resultat = CalculPaieMensuelle.calculer(
            activite = activiteRichmond,
            totalRecette = totalRecette,
            totalPaiement = paiementsAout.sum(),
            bonusApplicable = selection.palier
        )

        // Bonus attendu : 2 872 675 × 1% = 28 726.75
        assertEquals(28_726.75, resultat.montantBonus, 0.01)
        assertEquals(resultat.salaireMensuelBase + 28_726.75, resultat.salaireMensuelFinal, 0.01)
    }
}

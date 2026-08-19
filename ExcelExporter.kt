package com.lonaloto.data.export

import android.content.Context
import com.lonaloto.data.local.entities.Vente
import com.lonaloto.domain.calcul.ResultatPaieMensuelle
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Génère un fichier .xlsx reproduisant la structure du fichier Excel LONALOTO
 * d'origine (colonnes JOUR / DATE / RECETTE / PAIEMENT + section calcul de paie),
 * pour une activité et un mois donnés — facilite la comparaison directe pour
 * quelqu'un habitué à l'ancien fichier.
 */
object ExcelExporter {

    private val formatDate = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    fun exporterBilanMensuel(
        context: Context,
        nomActivite: String,
        libelleMois: String,
        ventes: List<Vente>,
        bilan: ResultatPaieMensuelle
    ): File {
        val workbook = XSSFWorkbook()
        val feuille = workbook.createSheet(nomActivite.take(31)) // 31 = limite Excel pour un nom de feuille

        val styleTitre = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true; fontHeightInPoints = 14 })
        }
        val styleEntete = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true })
        }

        var ligne = 0

        feuille.createRow(ligne++).createCell(0).apply { setCellValue("LONALOTO — $nomActivite — $libelleMois"); cellStyle = styleTitre }
        ligne++

        val enteteVentes = feuille.createRow(ligne++)
        listOf("JOUR", "DATE", "RECETTE JOURNALIÈRE (FCFA)", "PAIEMENT JOURNALIER (FCFA)").forEachIndexed { i, libelle ->
            enteteVentes.createCell(i).apply { setCellValue(libelle); cellStyle = styleEntete }
        }

        ventes.sortedBy { it.date }.forEachIndexed { index, vente ->
            val row = feuille.createRow(ligne++)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(formatDate.format(vente.date))
            row.createCell(2).setCellValue(vente.recette)
            row.createCell(3).setCellValue(vente.paiement)
        }

        ligne++
        feuille.createRow(ligne++).createCell(0).apply { setCellValue("CALCUL DE LA PAIE ET DES MARGES DU MOIS"); cellStyle = styleEntete }

        val lignesBilan = listOf(
            "TOTAL RECETTE" to bilan.totalRecette,
            "TOTAL PAIEMENT" to bilan.totalPaiement,
            "MONTANT TOTAL" to bilan.montantTotal,
            "MONTANT TOTAL LONACI" to bilan.montantTotalLonaci,
            "POINT PAIEMENT" to bilan.pointPaiement,
            "SALAIRE COUPEUR" to bilan.salaireCoupeur,
            "BONUS APPLIQUÉ" to bilan.montantBonus,
            "SALAIRE MENSUEL" to bilan.salaireMensuelFinal
        )
        lignesBilan.forEach { (libelle, valeur) ->
            val row = feuille.createRow(ligne++)
            row.createCell(0).setCellValue(libelle)
            row.createCell(2).setCellValue(valeur)
        }

        for (col in 0..3) feuille.autoSizeColumn(col)

        val dossierExports = File(context.cacheDir, "exports").apply { mkdirs() }
        val nomFichier = "LONALOTO_${nomActivite}_${libelleMois.replace(" ", "_")}.xlsx"
        val fichier = File(dossierExports, nomFichier)

        FileOutputStream(fichier).use { flux -> workbook.write(flux) }
        workbook.close()

        return fichier
    }
}

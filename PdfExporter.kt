package com.lonaloto.data.export

import android.content.Context
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.lonaloto.domain.calcul.ResultatPaieMensuelle
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/**
 * Génère un PDF de synthèse du bilan mensuel — format lisible, destiné à être
 * imprimé ou envoyé par email/WhatsApp à un responsable qui n'a pas l'app.
 */
object PdfExporter {

    private val ORANGE_LONALOTO = DeviceRgb(247, 127, 0)
    private val formatFcfa = NumberFormat.getNumberInstance(Locale.FRANCE)

    fun exporterBilanMensuel(
        context: Context,
        nomActivite: String,
        libelleMois: String,
        bilan: ResultatPaieMensuelle
    ): File {
        val dossierExports = File(context.cacheDir, "exports").apply { mkdirs() }
        val nomFichier = "LONALOTO_${nomActivite}_${libelleMois.replace(" ", "_")}.pdf"
        val fichier = File(dossierExports, nomFichier)

        PdfDocument(PdfWriter(fichier)).use { pdf ->
            val document = Document(pdf)

            document.add(
                Paragraph("LONALOTO")
                    .setFontSize(22f)
                    .setBold()
                    .setFontColor(ORANGE_LONALOTO)
            )
            document.add(Paragraph("Bilan mensuel — $nomActivite — $libelleMois").setFontSize(14f))
            document.add(Paragraph(" "))

            val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f))).useAllAvailableWidth()

            fun ligne(libelle: String, valeur: String, accent: Boolean = false) {
                table.addCell(
                    Cell().add(Paragraph(libelle).setBold(accent))
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                )
                table.addCell(
                    Cell().add(Paragraph(valeur).setBold(accent).setTextAlignment(TextAlignment.RIGHT))
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                )
            }

            ligne("Total recette", fcfa(bilan.totalRecette))
            ligne("Total paiement", fcfa(bilan.totalPaiement))
            ligne("Montant total", fcfa(bilan.montantTotal))
            ligne("Montant total LONACI", fcfa(bilan.montantTotalLonaci))
            ligne("Point paiement", fcfa(bilan.pointPaiement))
            ligne("Salaire coupeur", fcfa(bilan.salaireCoupeur))
            if (bilan.montantBonus > 0) {
                ligne("Bonus appliqué", fcfa(bilan.montantBonus))
            }
            ligne("Salaire mensuel", fcfa(bilan.salaireMensuelFinal), accent = true)

            document.add(table)
            document.add(Paragraph(" "))
            document.add(
                Paragraph("Document généré automatiquement par l'application LONALOTO.")
                    .setFontSize(9f)
                    .setFontColor(DeviceRgb(120, 120, 120))
            )

            document.close()
        }

        return fichier
    }

    private fun fcfa(valeur: Double): String = "${formatFcfa.format(valeur)} FCFA"
}

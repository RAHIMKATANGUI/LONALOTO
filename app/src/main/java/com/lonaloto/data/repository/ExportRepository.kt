package com.lonaloto.data.repository

import android.content.Context
import com.lonaloto.data.export.ExcelExporter
import com.lonaloto.data.export.PdfExporter
import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.dao.VenteDao
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.TypeAction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val venteDao: VenteDao,
    private val venteRepository: VenteRepository,
    private val historiqueAuditDao: HistoriqueAuditDao
) {

    suspend fun exporterExcel(
        activiteId: Long,
        moisId: Long,
        nomActivite: String,
        libelleMois: String,
        auteurId: Long
    ): File? {
        val bilan = venteRepository.bilanMensuel(activiteId, moisId) ?: return null
        val ventes = venteDao.parActiviteEtMois(activiteId, moisId)
        // On lit l'instantané courant du Flow — suffisant pour un export ponctuel.
        val listeVentes = ventes.first()

        val fichier = ExcelExporter.exporterBilanMensuel(context, nomActivite, libelleMois, listeVentes, bilan)

        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.EXPORT,
                tableCible = "ventes",
                nouvelleValeurJson = """{"format":"excel","activite":"$nomActivite","mois":"$libelleMois"}"""
            )
        )

        return fichier
    }

    suspend fun exporterPdf(
        activiteId: Long,
        moisId: Long,
        nomActivite: String,
        libelleMois: String,
        auteurId: Long
    ): File? {
        val bilan = venteRepository.bilanMensuel(activiteId, moisId) ?: return null
        val fichier = PdfExporter.exporterBilanMensuel(context, nomActivite, libelleMois, bilan)

        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.EXPORT,
                tableCible = "ventes",
                nouvelleValeurJson = """{"format":"pdf","activite":"$nomActivite","mois":"$libelleMois"}"""
            )
        )

        return fichier
    }
}

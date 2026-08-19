package com.lonaloto.ui.rapports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonaloto.data.local.dao.TotauxMensuels
import com.lonaloto.data.repository.ActiviteRepository
import com.lonaloto.data.repository.ExportRepository
import com.lonaloto.data.repository.MoisRepository
import com.lonaloto.data.repository.VenteRepository
import com.lonaloto.domain.auth.Permission
import com.lonaloto.domain.auth.Permissions
import com.lonaloto.domain.auth.SessionManager
import com.lonaloto.domain.calcul.ResultatPaieMensuelle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class EtatRapport(
    val chargement: Boolean = true,
    val libelleMois: String = "",
    val bilanMensuel: ResultatPaieMensuelle? = null,
    val bilanParSemaine: List<TotauxMensuels> = emptyList(),
    val messageErreur: String? = null,
    val peutExporter: Boolean = false,
    val fichierAExporter: File? = null
)

/**
 * Affiche le bilan mensuel (avec le détail de la paie, comme dans l'Excel)
 * et le découpage hebdomadaire (comme "Point Hebdomadaire"), pour l'activité
 * rattachée à l'utilisateur connecté.
 *
 * Un ADMIN verra ici l'activité qu'il choisit (sélecteur à ajouter dans un
 * prochain raffinement UI) ; pour l'instant l'écran suppose une activité
 * déjà déterminée par la session (CHEF_DE_FLOTTE / VENDEUR).
 */
@HiltViewModel
class RapportViewModel @Inject constructor(
    private val venteRepository: VenteRepository,
    private val moisRepository: MoisRepository,
    private val exportRepository: ExportRepository,
    private val activiteRepository: ActiviteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _etat = MutableStateFlow(EtatRapport())
    val etat: StateFlow<EtatRapport> = _etat.asStateFlow()

    private var activiteIdCourante: Long? = null
    private var moisIdCourant: Long? = null
    private var nomActiviteCourante: String = ""

    init {
        charger()
    }

    fun charger() {
        val session = sessionManager.utilisateurConnecte.value
        val activiteId = session?.activiteId
        if (session == null || activiteId == null) {
            _etat.value = EtatRapport(chargement = false, messageErreur = "Aucune activité rattachée à ce compte")
            return
        }

        val peutExporter = Permissions.autorise(session, Permission.EXPORT_PDF_EXCEL)

        viewModelScope.launch {
            _etat.value = _etat.value.copy(chargement = true, messageErreur = null)

            val mois = moisRepository.moisCourant(auteurId = session.id)
            activiteIdCourante = activiteId
            moisIdCourant = mois.id
            nomActiviteCourante = activiteRepository.parId(activiteId)?.nom ?: ""

            val bilan = venteRepository.bilanMensuel(activiteId, mois.id)
            val semaines = venteRepository.bilanHebdomadaire(
                activiteId = activiteId,
                moisId = mois.id,
                annee = mois.annee,
                numeroMoisCalendaire = mois.numeroMois
            )

            _etat.value = EtatRapport(
                chargement = false,
                libelleMois = mois.libelle,
                bilanMensuel = bilan,
                bilanParSemaine = semaines,
                peutExporter = peutExporter
            )
        }
    }

    fun exporterExcel() {
        val activiteId = activiteIdCourante ?: return
        val moisId = moisIdCourant ?: return
        val auteurId = sessionManager.utilisateurConnecte.value?.id ?: return

        viewModelScope.launch {
            val fichier = exportRepository.exporterExcel(activiteId, moisId, nomActiviteCourante, etat.value.libelleMois, auteurId)
            _etat.value = _etat.value.copy(fichierAExporter = fichier)
        }
    }

    fun exporterPdf() {
        val activiteId = activiteIdCourante ?: return
        val moisId = moisIdCourant ?: return
        val auteurId = sessionManager.utilisateurConnecte.value?.id ?: return

        viewModelScope.launch {
            val fichier = exportRepository.exporterPdf(activiteId, moisId, nomActiviteCourante, etat.value.libelleMois, auteurId)
            _etat.value = _etat.value.copy(fichierAExporter = fichier)
        }
    }

    fun fichierPartage() {
        _etat.value = _etat.value.copy(fichierAExporter = null)
    }
}

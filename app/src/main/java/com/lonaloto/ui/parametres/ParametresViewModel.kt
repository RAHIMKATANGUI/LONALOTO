package com.lonaloto.ui.parametres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.lonaloto.data.backup.ResultatRestauration
import com.lonaloto.data.backup.ResultatSauvegarde
import com.lonaloto.data.backup.SauvegardeManager
import com.lonaloto.data.local.entities.Activite
import com.lonaloto.data.local.entities.PalierBonus
import com.lonaloto.data.repository.*
import com.lonaloto.domain.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class EtatParametres(
    val activites: List<Activite> = emptyList(),
    val activiteSelectionneeId: Long? = null,
    val paliersDeLActiviteSelectionnee: List<PalierBonus> = emptyList(),
    val messageErreur: String? = null,
    val messageSucces: String? = null
)

@HiltViewModel
class ParametresViewModel @Inject constructor(
    private val activiteRepository: ActiviteRepository,
    private val palierBonusRepository: PalierBonusRepository,
    private val moisRepository: MoisRepository,
    private val sauvegardeManager: SauvegardeManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val activiteSelectionneeId = MutableStateFlow<Long?>(null)
    private val messages = MutableStateFlow<Pair<String?, String?>>(null to null)

    val etat: StateFlow<EtatParametres> = combine(
        activiteRepository.toutesActives(),
        activiteSelectionneeId,
        messages
    ) { activites, selectionId, (erreur, succes) ->
        val idEffectif = selectionId ?: activites.firstOrNull()?.id
        EtatParametres(
            activites = activites,
            activiteSelectionneeId = idEffectif,
            messageErreur = erreur,
            messageSucces = succes
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), EtatParametres())

    private val auteurId get() = sessionManager.utilisateurConnecte.value?.id ?: 0L

    fun selectionnerActivite(id: Long) {
        activiteSelectionneeId.value = id
    }

    fun creerActivite(nom: String, tauxRecette: Double, tauxPaiement: Double, tauxLonaci: Double, tauxSalaireCoupeur: Double) {
        viewModelScope.launch {
            activiteRepository.creer(
                Activite(
                    nom = nom,
                    tauxRecette = tauxRecette,
                    tauxPaiement = tauxPaiement,
                    tauxLonaci = tauxLonaci,
                    tauxSalaireCoupeur = tauxSalaireCoupeur
                ),
                auteurId = auteurId
            )
            messages.value = null to "Activité \"$nom\" créée"
        }
    }

    /** Renommer une activité (ex: ANGE → ANYAMA) sans toucher aux taux ni à l'historique. */
    fun renommerActivite(activiteId: Long, nouveauNom: String) {
        viewModelScope.launch {
            when (val resultat = activiteRepository.renommer(activiteId, nouveauNom, auteurId)) {
                is ResultatRenommage.Succes ->
                    messages.value = null to "\"${resultat.ancienNom}\" renommée en \"${resultat.nouveauNom}\""
                is ResultatRenommage.NomDejaUtilise ->
                    messages.value = "Le nom \"${resultat.nomConflictuel}\" est déjà utilisé" to null
                is ResultatRenommage.ActiviteIntrouvable ->
                    messages.value = "Activité introuvable" to null
            }
        }
    }

    fun modifierTaux(activite: Activite) {
        viewModelScope.launch {
            activiteRepository.modifierTaux(activite, auteurId)
            messages.value = null to "Taux mis à jour pour \"${activite.nom}\""
        }
    }

    fun ajouterPalier(activiteId: Long, seuilMin: Double, seuilMax: Double?, tauxBonus: Double) {
        viewModelScope.launch {
            palierBonusRepository.creer(
                PalierBonus(activiteId = activiteId, seuilMin = seuilMin, seuilMax = seuilMax, tauxBonus = tauxBonus),
                auteurId = auteurId
            )
            messages.value = null to "Palier de bonus ajouté"
        }
    }

    fun supprimerPalier(palier: PalierBonus) {
        viewModelScope.launch {
            palierBonusRepository.supprimer(palier, auteurId)
            messages.value = null to "Palier supprimé"
        }
    }

    fun paliersDeLActivite(activiteId: Long): StateFlow<List<PalierBonus>> =
        palierBonusRepository.parActivite(activiteId)
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    fun creerMoisSuivant(annee: Int, numeroMois: Int) {
        viewModelScope.launch {
            moisRepository.obtenirOuCreer(annee, numeroMois, auteurId)
            messages.value = null to "Mois créé (ou déjà existant)"
        }
    }

    fun exporterSauvegarde(): File? {
        return when (val resultat = sauvegardeManager.exporterVersFichierLocal()) {
            is ResultatSauvegarde.Succes -> {
                messages.value = null to "Sauvegarde créée : ${resultat.fichier.name}"
                resultat.fichier
            }
            is ResultatSauvegarde.Erreur -> {
                messages.value = resultat.message to null
                null
            }
        }
    }

    fun importerSauvegarde(uri: Uri, redemarrerApp: () -> Unit) {
        when (val resultat = sauvegardeManager.importerDepuisUri(uri)) {
            is ResultatRestauration.Succes -> {
                messages.value = null to "Sauvegarde restaurée. Redémarrage de l'application..."
                redemarrerApp()
            }
            is ResultatRestauration.Erreur -> {
                messages.value = resultat.message to null
            }
        }
    }
}

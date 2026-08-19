package com.lonaloto.ui.utilisateurs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonaloto.data.local.entities.Activite
import com.lonaloto.data.local.entities.Role
import com.lonaloto.data.local.entities.Utilisateur
import com.lonaloto.data.repository.ActiviteRepository
import com.lonaloto.data.repository.ResultatCreationUtilisateur
import com.lonaloto.data.repository.UtilisateurRepository
import com.lonaloto.domain.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EtatUtilisateurs(
    val utilisateurs: List<Utilisateur> = emptyList(),
    val activites: List<Activite> = emptyList(),
    val messageErreur: String? = null,
    val messageSucces: String? = null
)

@HiltViewModel
class UtilisateursViewModel @Inject constructor(
    private val utilisateurRepository: UtilisateurRepository,
    private val activiteRepository: ActiviteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val messages = MutableStateFlow<Pair<String?, String?>>(null to null)

    val etat: StateFlow<EtatUtilisateurs> = combine(
        utilisateurRepository.tousActifs(),
        activiteRepository.toutesActives(),
        messages
    ) { utilisateurs, activites, (erreur, succes) ->
        EtatUtilisateurs(utilisateurs, activites, erreur, succes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EtatUtilisateurs())

    fun creerUtilisateur(nom: String, pin: String, role: Role, activiteId: Long?) {
        val auteurId = sessionManager.utilisateurConnecte.value?.id ?: return
        viewModelScope.launch {
            when (val resultat = utilisateurRepository.creer(nom, pin, role, activiteId, auteurId)) {
                is ResultatCreationUtilisateur.Succes ->
                    messages.value = null to "Utilisateur \"$nom\" créé"
                is ResultatCreationUtilisateur.NomDejaUtilise ->
                    messages.value = "Ce nom est déjà utilisé" to null
                is ResultatCreationUtilisateur.Erreur ->
                    messages.value = resultat.message to null
            }
        }
    }

    fun desactiver(utilisateurId: Long) {
        val auteurId = sessionManager.utilisateurConnecte.value?.id ?: return
        viewModelScope.launch {
            utilisateurRepository.desactiver(utilisateurId, auteurId)
            messages.value = null to "Utilisateur désactivé"
        }
    }

    fun reinitialiserPin(utilisateurId: Long, nouveauPin: String) {
        val auteurId = sessionManager.utilisateurConnecte.value?.id ?: return
        viewModelScope.launch {
            when (val resultat = utilisateurRepository.reinitialiserPin(utilisateurId, nouveauPin, auteurId)) {
                is ResultatCreationUtilisateur.Succes -> messages.value = null to "PIN réinitialisé"
                is ResultatCreationUtilisateur.Erreur -> messages.value = resultat.message to null
                else -> Unit
            }
        }
    }
}

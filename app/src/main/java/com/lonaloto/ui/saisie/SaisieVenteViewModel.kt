package com.lonaloto.ui.saisie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonaloto.data.repository.MoisRepository
import com.lonaloto.data.repository.ResultatSaisie
import com.lonaloto.data.repository.VenteRepository
import com.lonaloto.domain.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class EtatSaisie(
    val recette: String = "",
    val paiement: String = "",
    val enCours: Boolean = false,
    val messageErreur: String? = null,
    val messageSucces: String? = null
)

/**
 * Gère la saisie de la recette/paiement du jour pour le VENDEUR connecté.
 * (Le CHEF_DE_FLOTTE utilisera un écran similaire avec sélection du vendeur —
 * réutilise le même repository, seule l'UI diffère légèrement.)
 */
@HiltViewModel
class SaisieVenteViewModel @Inject constructor(
    private val venteRepository: VenteRepository,
    private val moisRepository: MoisRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _etat = MutableStateFlow(EtatSaisie())
    val etat: StateFlow<EtatSaisie> = _etat.asStateFlow()

    fun onRecetteChange(valeur: String) {
        _etat.value = _etat.value.copy(recette = filtrerMontant(valeur), messageErreur = null, messageSucces = null)
    }

    fun onPaiementChange(valeur: String) {
        _etat.value = _etat.value.copy(paiement = filtrerMontant(valeur), messageErreur = null, messageSucces = null)
    }

    private fun filtrerMontant(valeur: String): String = valeur.filter { it.isDigit() }.take(9)

    fun enregistrer() {
        val session = sessionManager.utilisateurConnecte.value
        if (session == null) {
            _etat.value = _etat.value.copy(messageErreur = "Session expirée, reconnectez-vous")
            return
        }
        val activiteId = session.activiteId
        if (activiteId == null) {
            _etat.value = _etat.value.copy(messageErreur = "Aucune activité rattachée à ce compte")
            return
        }

        val recette = _etat.value.recette.toDoubleOrNull()
        val paiement = _etat.value.paiement.toDoubleOrNull()
        if (recette == null || paiement == null) {
            _etat.value = _etat.value.copy(messageErreur = "Recette et paiement doivent être des nombres valides")
            return
        }

        viewModelScope.launch {
            _etat.value = _etat.value.copy(enCours = true, messageErreur = null, messageSucces = null)

            val mois = moisRepository.moisCourant(auteurId = session.id)

            when (val resultat = venteRepository.saisir(
                activiteId = activiteId,
                moisId = mois.id,
                vendeurId = session.id,
                date = Date(),
                recette = recette,
                paiement = paiement,
                saisiParId = session.id
            )) {
                is ResultatSaisie.Succes -> {
                    _etat.value = EtatSaisie(messageSucces = "Saisie enregistrée avec succès")
                }
                is ResultatSaisie.MoisCloture -> {
                    _etat.value = _etat.value.copy(
                        enCours = false,
                        messageErreur = "Ce mois est clôturé, la saisie n'est plus possible"
                    )
                }
                is ResultatSaisie.Erreur -> {
                    _etat.value = _etat.value.copy(enCours = false, messageErreur = resultat.message)
                }
            }
        }
    }
}

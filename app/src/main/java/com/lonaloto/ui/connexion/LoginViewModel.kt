package com.lonaloto.ui.connexion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonaloto.data.repository.AuthRepository
import com.lonaloto.data.repository.ResultatConnexion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EtatConnexion(
    val nom: String = "",
    val pin: String = "",
    val enCours: Boolean = false,
    val messageErreur: String? = null,
    val connexionReussie: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _etat = MutableStateFlow(EtatConnexion())
    val etat: StateFlow<EtatConnexion> = _etat.asStateFlow()

    fun onNomChange(valeur: String) {
        _etat.value = _etat.value.copy(nom = valeur, messageErreur = null)
    }

    fun onPinChange(valeur: String) {
        // Le clavier numérique ne devrait produire que des chiffres, mais on filtre
        // par sécurité et on plafonne à 8 caractères (cohérent avec PinHasher).
        val filtre = valeur.filter { it.isDigit() }.take(8)
        _etat.value = _etat.value.copy(pin = filtre, messageErreur = null)
    }

    fun seConnecter() {
        val etatActuel = _etat.value
        if (etatActuel.nom.isBlank() || etatActuel.pin.length < 4) {
            _etat.value = etatActuel.copy(messageErreur = "Nom et code PIN (4 chiffres minimum) requis")
            return
        }

        viewModelScope.launch {
            _etat.value = etatActuel.copy(enCours = true, messageErreur = null)

            when (val resultat = authRepository.connecter(etatActuel.nom, etatActuel.pin)) {
                is ResultatConnexion.Succes -> {
                    _etat.value = _etat.value.copy(enCours = false, connexionReussie = true)
                }
                is ResultatConnexion.IdentifiantsIncorrects -> {
                    _etat.value = _etat.value.copy(
                        enCours = false,
                        pin = "",
                        messageErreur = "Nom ou code PIN incorrect"
                    )
                }
                is ResultatConnexion.CompteDesactive -> {
                    _etat.value = _etat.value.copy(
                        enCours = false,
                        pin = "",
                        messageErreur = "Ce compte a été désactivé. Contactez l'administrateur."
                    )
                }
            }
        }
    }
}

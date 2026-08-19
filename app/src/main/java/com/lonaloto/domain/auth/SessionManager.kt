package com.lonaloto.domain.auth

import com.lonaloto.data.local.entities.Role
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utilisateur actuellement connecté, tel qu'exposé au reste de l'app.
 * Volontairement minimal : pas de pinHash ici, pour éviter toute fuite accidentelle.
 */
data class SessionUtilisateur(
    val id: Long,
    val nom: String,
    val role: Role,
    val activiteId: Long?
)

/**
 * Gère la session en cours, uniquement en mémoire (RAM) — jamais persistée sur disque.
 * Fermer l'app ou tuer le processus déconnecte automatiquement l'utilisateur :
 * comportement volontaire pour un usage terrain multi-utilisateurs sur le même appareil.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _utilisateurConnecte = MutableStateFlow<SessionUtilisateur?>(null)
    val utilisateurConnecte: StateFlow<SessionUtilisateur?> = _utilisateurConnecte.asStateFlow()

    fun ouvrirSession(utilisateur: SessionUtilisateur) {
        _utilisateurConnecte.value = utilisateur
    }

    fun fermerSession() {
        _utilisateurConnecte.value = null
    }

    fun estConnecte(): Boolean = _utilisateurConnecte.value != null
}

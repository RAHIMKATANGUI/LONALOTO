package com.lonaloto.data.repository

import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.dao.UtilisateurDao
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.TypeAction
import com.lonaloto.domain.auth.PinHasher
import com.lonaloto.domain.auth.SessionManager
import com.lonaloto.domain.auth.SessionUtilisateur
import javax.inject.Inject
import javax.inject.Singleton

sealed class ResultatConnexion {
    data class Succes(val session: SessionUtilisateur) : ResultatConnexion()
    data object IdentifiantsIncorrects : ResultatConnexion()
    data object CompteDesactive : ResultatConnexion()
}

@Singleton
class AuthRepository @Inject constructor(
    private val utilisateurDao: UtilisateurDao,
    private val historiqueAuditDao: HistoriqueAuditDao,
    private val sessionManager: SessionManager
) {

    /**
     * Tente une connexion par Nom + PIN.
     * Le message d'erreur reste volontairement générique ("identifiants incorrects")
     * quel que soit le problème (nom inconnu ou PIN faux), pour ne pas révéler
     * si un nom d'utilisateur existe.
     */
    suspend fun connecter(nom: String, pin: String): ResultatConnexion {
        val utilisateur = utilisateurDao.parNom(nom.trim())
            ?: return ResultatConnexion.IdentifiantsIncorrects

        if (!utilisateur.actif) {
            return ResultatConnexion.CompteDesactive
        }

        if (!PinHasher.verifier(pin, utilisateur.pinHash)) {
            return ResultatConnexion.IdentifiantsIncorrects
        }

        val session = SessionUtilisateur(
            id = utilisateur.id,
            nom = utilisateur.nom,
            role = utilisateur.role,
            activiteId = utilisateur.activiteId
        )
        sessionManager.ouvrirSession(session)

        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = utilisateur.id,
                action = TypeAction.CONNEXION,
                tableCible = "utilisateurs",
                enregistrementId = utilisateur.id
            )
        )

        return ResultatConnexion.Succes(session)
    }

    fun deconnecter() {
        sessionManager.fermerSession()
    }
}

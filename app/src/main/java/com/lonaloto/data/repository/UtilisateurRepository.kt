package com.lonaloto.data.repository

import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.dao.UtilisateurDao
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.Role
import com.lonaloto.data.local.entities.TypeAction
import com.lonaloto.data.local.entities.Utilisateur
import com.lonaloto.domain.auth.PinHasher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ResultatCreationUtilisateur {
    data class Succes(val utilisateurId: Long) : ResultatCreationUtilisateur()
    data object NomDejaUtilise : ResultatCreationUtilisateur()
    data class Erreur(val message: String) : ResultatCreationUtilisateur()
}

@Singleton
class UtilisateurRepository @Inject constructor(
    private val utilisateurDao: UtilisateurDao,
    private val historiqueAuditDao: HistoriqueAuditDao
) {

    fun tousActifs(): Flow<List<Utilisateur>> = utilisateurDao.tousActifs()

    fun parActivite(activiteId: Long): Flow<List<Utilisateur>> = utilisateurDao.parActivite(activiteId)

    /**
     * Crée un nouvel utilisateur (ADMIN uniquement, vérifié côté UI/navigation).
     * - Le PIN est haché avant stockage (jamais en clair).
     * - Un ADMIN n'a pas d'activité rattachée (activiteId = null) ; CHEF_DE_FLOTTE
     *   et VENDEUR doivent obligatoirement être rattachés à une activité.
     */
    suspend fun creer(
        nom: String,
        pin: String,
        role: Role,
        activiteId: Long?,
        auteurId: Long
    ): ResultatCreationUtilisateur {
        val nomNettoye = nom.trim()
        if (nomNettoye.isBlank()) {
            return ResultatCreationUtilisateur.Erreur("Le nom ne peut pas être vide")
        }
        if (role != Role.ADMIN && activiteId == null) {
            return ResultatCreationUtilisateur.Erreur("Une activité doit être choisie pour ce rôle")
        }
        if (utilisateurDao.parNom(nomNettoye) != null) {
            return ResultatCreationUtilisateur.NomDejaUtilise
        }

        val pinHash = try {
            PinHasher.hacher(pin)
        } catch (e: IllegalArgumentException) {
            return ResultatCreationUtilisateur.Erreur(e.message ?: "PIN invalide")
        }

        val utilisateur = Utilisateur(
            nom = nomNettoye,
            pinHash = pinHash,
            role = role,
            activiteId = activiteId
        )
        val id = utilisateurDao.inserer(utilisateur)

        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.CREATION,
                tableCible = "utilisateurs",
                enregistrementId = id,
                nouvelleValeurJson = """{"nom":"$nomNettoye","role":"${role.name}"}"""
            )
        )

        return ResultatCreationUtilisateur.Succes(id)
    }

    /** Désactivation logique — jamais de suppression physique (traçabilité conservée). */
    suspend fun desactiver(utilisateurId: Long, auteurId: Long) {
        utilisateurDao.desactiver(utilisateurId)
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.SUPPRESSION,
                tableCible = "utilisateurs",
                enregistrementId = utilisateurId,
                nouvelleValeurJson = """{"actif":false}"""
            )
        )
    }

    /** Réinitialisation du PIN par l'ADMIN (utilisateur qui a oublié son code). */
    suspend fun reinitialiserPin(utilisateurId: Long, nouveauPin: String, auteurId: Long): ResultatCreationUtilisateur {
        val utilisateur = utilisateurDao.parId(utilisateurId)
            ?: return ResultatCreationUtilisateur.Erreur("Utilisateur introuvable")

        val pinHash = try {
            PinHasher.hacher(nouveauPin)
        } catch (e: IllegalArgumentException) {
            return ResultatCreationUtilisateur.Erreur(e.message ?: "PIN invalide")
        }

        utilisateurDao.modifier(utilisateur.copy(pinHash = pinHash))
        historiqueAuditDao.inserer(
            HistoriqueAudit(
                utilisateurId = auteurId,
                action = TypeAction.MODIFICATION,
                tableCible = "utilisateurs",
                enregistrementId = utilisateurId,
                nouvelleValeurJson = """{"pin":"reinitialise"}"""
            )
        )
        return ResultatCreationUtilisateur.Succes(utilisateurId)
    }
}

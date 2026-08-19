package com.lonaloto.domain.auth

import com.lonaloto.data.local.entities.Role

/**
 * Centralise la matrice de permissions définie dans l'architecture (Étape 3).
 * Toute vérification de droit dans l'UI ou les repositories doit passer par ici,
 * pour éviter que la logique de permissions se disperse dans le code.
 */
enum class Permission {
    SAISIR_VENTES_PERSONNELLES,
    SAISIR_VENTES_FLOTTE,
    VALIDER_SAISIES_FLOTTE,
    VOIR_BILAN_PERSONNEL,
    VOIR_BILAN_FLOTTE,
    VOIR_TOUS_LES_RAPPORTS,
    GERER_UTILISATEURS,
    MODIFIER_TAUX,
    GERER_MOIS,
    GERER_ACTIVITES,
    GERER_PALIERS_BONUS,
    EXPORT_PDF_EXCEL,
    VOIR_JOURNAL_AUDIT,
    EXPORT_IMPORT_BASE
}

object Permissions {

    private val matrice: Map<Role, Set<Permission>> = mapOf(
        Role.ADMIN to Permission.entries.toSet(), // accès total

        Role.CHEF_DE_FLOTTE to setOf(
            Permission.SAISIR_VENTES_PERSONNELLES,
            Permission.SAISIR_VENTES_FLOTTE,
            Permission.VALIDER_SAISIES_FLOTTE,
            Permission.VOIR_BILAN_PERSONNEL,
            Permission.VOIR_BILAN_FLOTTE,
            Permission.EXPORT_PDF_EXCEL
        ),

        Role.VENDEUR to setOf(
            Permission.SAISIR_VENTES_PERSONNELLES,
            Permission.VOIR_BILAN_PERSONNEL
        )
    )

    fun autorise(role: Role, permission: Permission): Boolean =
        matrice[role]?.contains(permission) == true

    fun autorise(session: SessionUtilisateur?, permission: Permission): Boolean =
        session != null && autorise(session.role, permission)
}

package com.lonaloto.domain.auth

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Hash et vérification du code PIN.
 * Le PIN n'est JAMAIS stocké ni comparé en clair — uniquement son hash BCrypt,
 * stocké dans Utilisateur.pinHash.
 *
 * BCrypt est volontairement lent (protection contre le brute-force) mais reste
 * instantané côté UX pour un unique login sur un téléphone terrain.
 */
object PinHasher {

    private const val COUT_BCRYPT = 12

    /** À utiliser lors de la création/modification d'un utilisateur. */
    fun hacher(pin: String): String {
        require(pin.length in 4..8 && pin.all { it.isDigit() }) {
            "Le PIN doit contenir entre 4 et 8 chiffres"
        }
        return BCrypt.withDefaults().hashToString(COUT_BCRYPT, pin.toCharArray())
    }

    /** À utiliser lors de la tentative de connexion. */
    fun verifier(pinSaisi: String, hashStocke: String): Boolean {
        return BCrypt.verifyer().verify(pinSaisi.toCharArray(), hashStocke).verified
    }
}

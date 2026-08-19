package com.lonaloto.domain.calcul

/**
 * Reproduit exactement le découpage de la feuille Excel "Point Hebdomadaire" :
 * blocs fixes de 7 jours calendaires — 1-7, 8-14, 15-21, 22-28, 29-fin —
 * quel que soit le jour de la semaine réel (ce ne sont PAS des semaines lundi-dimanche).
 */
object CalculSemaine {

    /** Numéro de semaine (1 à 5) pour un jour du mois donné (1 à 31). */
    fun numeroSemaine(jourDuMois: Int): Int {
        require(jourDuMois in 1..31) { "jourDuMois doit être entre 1 et 31" }
        return ((jourDuMois - 1) / 7) + 1
    }

    /** Bornes (premier jour, dernier jour) d'une semaine donnée, dans un mois de `joursDansMois` jours. */
    fun bornes(numeroSemaine: Int, joursDansMois: Int): IntRange {
        require(numeroSemaine in 1..5) { "numeroSemaine doit être entre 1 et 5" }
        val debut = (numeroSemaine - 1) * 7 + 1
        val fin = minOf(numeroSemaine * 7, joursDansMois)
        return debut..fin
    }

    /** Nombre de semaines utiles pour un mois de `joursDansMois` jours (4 ou 5). */
    fun nombreSemaines(joursDansMois: Int): Int = numeroSemaine(joursDansMois)
}

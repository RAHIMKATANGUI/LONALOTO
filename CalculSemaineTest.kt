package com.lonaloto.domain.calcul

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculSemaineTest {

    @Test
    fun `le decoupage reproduit exactement les blocs de la feuille Point Hebdomadaire`() {
        // Semaine 1 : jours 1 à 7
        assertEquals(1, CalculSemaine.numeroSemaine(1))
        assertEquals(1, CalculSemaine.numeroSemaine(7))
        // Semaine 2 : jours 8 à 14
        assertEquals(2, CalculSemaine.numeroSemaine(8))
        assertEquals(2, CalculSemaine.numeroSemaine(14))
        // Semaine 3 : jours 15 à 21
        assertEquals(3, CalculSemaine.numeroSemaine(15))
        assertEquals(3, CalculSemaine.numeroSemaine(21))
        // Semaine 4 : jours 22 à 28
        assertEquals(4, CalculSemaine.numeroSemaine(22))
        assertEquals(4, CalculSemaine.numeroSemaine(28))
        // Semaine 5 : jours 29 à 31
        assertEquals(5, CalculSemaine.numeroSemaine(29))
        assertEquals(5, CalculSemaine.numeroSemaine(31))
    }

    @Test
    fun `les bornes de la semaine 5 s'arretent au dernier jour du mois`() {
        // Août = 31 jours → semaine 5 va de 29 à 31
        assertEquals(29..31, CalculSemaine.bornes(5, joursDansMois = 31))
        // Avril = 30 jours → semaine 5 va de 29 à 30
        assertEquals(29..30, CalculSemaine.bornes(5, joursDansMois = 30))
        // Février (non bissextile) = 28 jours → pas de semaine 5, semaine 4 = 22-28
        assertEquals(22..28, CalculSemaine.bornes(4, joursDansMois = 28))
    }

    @Test
    fun `un mois de 28 jours n'a que 4 semaines`() {
        assertEquals(4, CalculSemaine.nombreSemaines(joursDansMois = 28))
    }

    @Test
    fun `un mois de 31 jours a 5 semaines`() {
        assertEquals(5, CalculSemaine.nombreSemaines(joursDansMois = 31))
    }
}

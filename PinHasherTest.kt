package com.lonaloto.domain.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun `un PIN correct est verifie avec succes`() {
        val hash = PinHasher.hacher("1234")
        assertTrue(PinHasher.verifier("1234", hash))
    }

    @Test
    fun `un PIN incorrect est rejete`() {
        val hash = PinHasher.hacher("1234")
        assertFalse(PinHasher.verifier("9999", hash))
    }

    @Test
    fun `le hash ne contient jamais le PIN en clair`() {
        val pin = "5678"
        val hash = PinHasher.hacher(pin)
        assertNotEquals(pin, hash)
        assertTrue(hash.length > pin.length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un PIN trop court est refuse`() {
        PinHasher.hacher("12")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un PIN non numerique est refuse`() {
        PinHasher.hacher("abcd")
    }
}

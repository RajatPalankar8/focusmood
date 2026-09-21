package com.proto.focusonwork.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinManagerTest {
    @Test
    fun pinFormat_requiresFourDigits() {
        val valid = "1234"
        assertTrue(valid.length == 4 && valid.all(Char::isDigit))
        assertFalse("12ab".length == 4 && "12ab".all(Char::isDigit))
    }
}

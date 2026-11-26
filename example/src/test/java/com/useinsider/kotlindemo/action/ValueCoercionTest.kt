package com.useinsider.kotlindemo.action

import com.useinsider.insider.InsiderGender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValueCoercionTest {

    @Test
    fun parseBirthdayReadsIsoDatesAsUtcMidnightAndRejectsTheRest() {
        assertEquals(631152000000L, parseBirthday("1990-01-01")?.time)
        assertEquals(631152000000L, parseBirthday(" 1990-01-01 ")?.time)
        assertNull(parseBirthday(""))
        assertNull(parseBirthday("01/01/1990"))
        assertNull(parseBirthday("1990-02-30"))
    }

    @Test
    fun coerceIntParsesOrFallsBackToZero() {
        assertEquals(42, coerceInt("42"))
        assertEquals(-7, coerceInt("-7"))
        assertEquals(0, coerceInt(""))
        assertEquals(0, coerceInt("4.2"))
        assertEquals(0, coerceInt("abc"))
    }

    @Test
    fun coerceDoubleParsesOrFallsBackToZero() {
        assertEquals(4.2, coerceDouble("4.2"), 0.0)
        assertEquals(3.0, coerceDouble("3"), 0.0)
        assertEquals(0.0, coerceDouble(""), 0.0)
        assertEquals(0.0, coerceDouble("abc"), 0.0)
    }

    @Test
    fun coerceBooleanIsStrictAndFallsBackToFalse() {
        assertTrue(coerceBoolean("true"))
        assertFalse(coerceBoolean("false"))
        assertFalse(coerceBoolean("TRUE"))
        assertFalse(coerceBoolean("1"))
        assertFalse(coerceBoolean(""))
    }

    @Test
    fun mapGenderAcceptsNamesCaseInsensitivelyAndIndices() {
        assertEquals(InsiderGender.MALE, mapGender("male"))
        assertEquals(InsiderGender.MALE, mapGender("MALE"))
        assertEquals(InsiderGender.MALE, mapGender("0"))
        assertEquals(InsiderGender.FEMALE, mapGender("Female"))
        assertEquals(InsiderGender.FEMALE, mapGender("1"))
    }

    @Test
    fun mapGenderFallsBackToOther() {
        assertEquals(InsiderGender.OTHER, mapGender(""))
        assertEquals(InsiderGender.OTHER, mapGender("2"))
        assertEquals(InsiderGender.OTHER, mapGender("unknown"))
    }
}

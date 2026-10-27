package com.appsonair.appremark.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class RemarkTypeMapperTest {

    @Test
    fun improvementSuggestion_mapsToSuggestion() {
        assertEquals("SUGGESTION", RemarkTypeMapper.getType("Improvement suggestion"))
    }

    @Test
    fun bugReport_mapsToBug() {
        assertEquals("BUG", RemarkTypeMapper.getType("Bug report"))
    }

    @Test
    fun unknownType_fallsBackToSuggestion() {
        assertEquals("SUGGESTION", RemarkTypeMapper.getType("Something else"))
        assertEquals("SUGGESTION", RemarkTypeMapper.getType(""))
    }

    @Test
    fun matchingIsCaseSensitive() {
        assertEquals("SUGGESTION", RemarkTypeMapper.getType("bug report"))
        assertEquals("SUGGESTION", RemarkTypeMapper.getType("BUG REPORT"))
    }

    @Test(expected = NullPointerException::class)
    fun nullType_throws() {
        RemarkTypeMapper.getType(null)
    }
}

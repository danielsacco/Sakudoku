package com.des.sakudoku

import com.des.sakudoku.model.CellData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CellDataTest {

    @Test
    fun test1() {
        val candidates = setOf(1, 5, 9)
        val sut = CellData.CellCandidates(candidates)

        assertTrue(sut.isCandidate(1))
        assertFalse(sut.isCandidate(4))
    }

    @Test
    fun test2() {
        val sut = CellData.CellCandidates()

        assertFalse(sut.isCandidate(1))
        assertFalse(sut.isCandidate(4))

        assertTrue(sut.isEmpty())
    }



}
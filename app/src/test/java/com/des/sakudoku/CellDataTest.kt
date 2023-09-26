package com.des.sakudoku

import com.des.sakudoku.model.CellData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CellDataTest {

    @Test
    fun test1() {
        val candidates = setOf(1, 5, 9)
        val sut = CellData.PlayerCellData(1,1, options = candidates)

        assertTrue(sut.isCandidate(1))
        assertFalse(sut.isCandidate(4))
    }

    @Test
    fun test2() {
        val sut = CellData.PlayerCellData(1, 1)

        assertFalse(sut.isCandidate(1))
        assertFalse(sut.isCandidate(4))

        assertTrue(sut.isEmpty())
    }

    // TODO Other Tests !!!


}
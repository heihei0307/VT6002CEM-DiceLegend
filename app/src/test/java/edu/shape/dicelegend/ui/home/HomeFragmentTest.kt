package edu.shape.dicelegend.ui.home

import edu.shape.dicelegend.utils.DiceUtil
import org.junit.Assert.assertEquals
import org.junit.Test

internal class HomeFragmentTest{
    private val testExtra: DiceUtil.Companion = DiceUtil

    @Test
    fun testPlayerWin(){
        val playerDice = 2
        val aiDice = 1
        val expected = 1
        assertEquals(expected, testExtra.compareNumber(playerDice, aiDice))
    }

    @Test
    fun testAiWin(){
        val playerDice = 3
        val aiDice = 5
        val expected = 2
        assertEquals(expected, testExtra.compareNumber(playerDice, aiDice))
    }

    @Test
    fun testDraw(){
        val playerDice = 2
        val aiDice = 2
        val expected = 0
        assertEquals(expected, testExtra.compareNumber(playerDice, aiDice))
    }
}
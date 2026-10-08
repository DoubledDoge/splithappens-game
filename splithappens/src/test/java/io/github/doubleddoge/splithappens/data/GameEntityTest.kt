package io.github.doubleddoge.splithappens.data

import io.github.doubleddoge.splithappens.data.entity.GameEntity
import org.junit.Assert
import org.junit.Test

class GameEntityTest {
    @Test
    fun create_withoutInsurance_hasZeroInsurance() {
        val game = GameEntity.create(sessionId = 1, gameNum = 1, dealerTotal = 20)
        Assert.assertEquals(0L, game.insuranceBet)
        Assert.assertEquals(0L, game.insurancePayout)
    }

    @Test
    fun create_dealerBlackjack_paysThreeTimesInsurance() {
        val game =
            GameEntity.create(1, 1, 21, insuranceBet = 50, dealerHadBlackjack = true)
        Assert.assertEquals(50L, game.insuranceBet)
        Assert.assertEquals(150L, game.insurancePayout)
    }

    @Test
    fun create_noDealerBlackjack_insuranceLost() {
        val game =
            GameEntity.create(1, 1, 19, insuranceBet = 50, dealerHadBlackjack = false)
        Assert.assertEquals(0L, game.insurancePayout)
    }

    @Test(expected = IllegalArgumentException::class)
    fun create_negativeInsurance_throws() {
        GameEntity.create(1, 1, 18, insuranceBet = -1)
    }

    @Test
    fun create_keepsIdsAndTotals_andLeavesGameIdUnassigned() {
        val game = GameEntity.create(sessionId = 7, gameNum = 3, dealerTotal = 26)
        Assert.assertEquals(0L, game.gameId)
        Assert.assertEquals(7L, game.sessionId)
        Assert.assertEquals(3, game.gameNum)
        Assert.assertEquals(26, game.dealerTotal)
    }
}
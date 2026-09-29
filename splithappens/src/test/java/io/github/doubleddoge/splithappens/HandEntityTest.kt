package io.github.doubleddoge.splithappens

import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertEquals

class HandEntityTest {
    @Test fun bustedHandMustLose(){
        assertThrows(IllegalArgumentException::class.java){
            HandEntity.player(10, HandStatus.BUSTED, HandResult.WIN)
        }
    }
    @Test fun DealerCannotSurrender(){
        assertThrows(IllegalArgumentException::class.java){
            HandEntity.dealer(HandStatus.SURRENDERED)
        }

    }
    @Test
    fun payoutIsDerivedFromResult(){
        val hand= HandEntity.player(10, HandStatus.STOOD, HandResult.WIN)
        assertEquals(20L, hand.payout)
    }
}
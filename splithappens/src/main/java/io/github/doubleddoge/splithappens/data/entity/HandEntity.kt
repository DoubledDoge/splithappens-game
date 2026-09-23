package io.github.doubleddoge.splithappens.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.doubleddoge.splithappens.data.enum.HandOwner
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus

// Net profit can't be derived from bet x result
@Entity(
    tableName = "Hands",
    foreignKeys =
        [
            ForeignKey(
                entity = GameEntity::class,
                parentColumns = ["gameId"],
                childColumns = ["gameId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = HandEntity::class,
                parentColumns = ["handId"],
                childColumns = ["parentHandId"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index("gameId"), Index("parentHandId")],
)
data class HandEntity(
    @PrimaryKey(autoGenerate = true) val handId: Long = 0,
    val gameId: Long,
    val parentHandId: Long? = null,
    val owner: HandOwner,
    val betAmount: Long,
    val isDoubled: Boolean = false,
    val handStatus: HandStatus,
    val result: HandResult?, // Could be null for dealer hands
    val payout: Long = 0, // Deterministic based on HandResult
) {
    companion object {
        // IDs intentionally kept as placeholders (0/null) where RoundDao.saveRound fills them in
        fun player(
            betAmount: Long,
            handStatus: HandStatus,
            result: HandResult,
            isDoubled: Boolean = false,
        ): HandEntity {
            require(handStatus != HandStatus.BUSTED || result == HandResult.LOSS) {
                "A busted hand always loses"
            }
            require((handStatus == HandStatus.SURRENDERED) == (result == HandResult.SURRENDER)) {
                "SURRENDERED status and SURRENDER result must go together"
            }
            require(!isDoubled || handStatus == HandStatus.STOOD || handStatus == HandStatus.BUSTED) {
                "A doubled hand takes one card and ends, so it can only be STOOD or BUSTED"
            }
            return HandEntity(
                gameId = 0,
                owner = HandOwner.PLAYER,
                betAmount = betAmount,
                isDoubled = isDoubled,
                handStatus = handStatus,
                result = result,
                payout = result.payoutFor(betAmount),
            )
        }

        // Stats queries must filter on owner (or result IS NOT NULL)
        fun dealer(handStatus: HandStatus): HandEntity {
            require(handStatus != HandStatus.SURRENDERED) { "The dealer cannot surrender" }
            return HandEntity(
                gameId = 0,
                owner = HandOwner.DEALER,
                betAmount = 0,
                handStatus = handStatus,
                result = null,
                payout = 0,
            )
        }
    }
}

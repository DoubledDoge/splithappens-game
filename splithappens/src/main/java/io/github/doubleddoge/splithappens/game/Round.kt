package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.dao.HandRecord
import io.github.doubleddoge.splithappens.data.dao.RoundRecord
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.HandStatus

enum class Action {
  HIT,
  STAND,
  DOUBLE,
  SPLIT,
  SURRENDER,
}

enum class Phase {
  BETTING,
  INSURANCE,
  PLAYER_TURN,
  FINISHED,
}

class PlayerHand(
    val cards: MutableList<Card>,
    var bet: Long,
    val parentIndex: Int? = null,
) {
  var afterSplit = parentIndex != null
  var isDoubled = false
  var stood = false
  var surrendered = false

  val value
    get() = HandValue(cards, isSplit = afterSplit)

  val isDone
    get() = stood || surrendered || isDoubled || value.total >= 21

  fun status(): HandStatus =
      when {
        surrendered -> HandStatus.SURRENDERED
        value.isBust -> HandStatus.BUSTED
        value.isBlackjack -> HandStatus.BLACKJACK
        else -> HandStatus.STOOD
      }

  fun snapshot(): PlayerHand =
      PlayerHand(cards.toMutableList(), bet, parentIndex).also {
        it.afterSplit = afterSplit
        it.isDoubled = isDoubled
        it.stood = stood
        it.surrendered = surrendered
      }
}

fun List<PlayerHand>.snapshot(): List<PlayerHand> = map { it.snapshot() }

// One Round. Bets do NOT deduct funds on placement, Bankroll is the balance at round start.
// Reshuffles between rounds.

class Round(
    private val shoe: Shoe,
    private val mode: GameMode,
    private val bankroll: Long,
    private val minBet: Long = 0,
    private val maxHands: Int = 4,
    private val autoStandOn21: Boolean = true,
) {
  var phase = Phase.BETTING
    private set

  val hands = mutableListOf<PlayerHand>()
  val dealerCards = mutableListOf<Card>() // index 1 is the hidden "hole" card.
  var insuranceBet = 0L
    private set

  var activeIndex = 0
    private set

  val holeHidden
    get() = phase != Phase.FINISHED

  val dealerValue
    get() = dealerCards.evaluateHand()

  private val available
    get() = bankroll - hands.sumOf { it.bet } - insuranceBet

  fun startBetting(amount: Long) {
    check(phase == Phase.BETTING) { "Bet already placed" }
    require(amount in minBet..bankroll) { "Bet must be between $minBet and $bankroll" }
    hands += PlayerHand(mutableListOf(), amount)
  }

  fun dealOneToPlayer() {
    check(hands.isNotEmpty()) { "Must call startBetting first" }
    hands[0].cards += shoe.deal()
  }

  fun dealOneToDealer() {
    dealerCards += shoe.deal()
  }

  fun finalizeDeal() {
    check(hands.isNotEmpty() && hands[0].cards.size == 2 && dealerCards.size == 2) {
      "Initial deal is incomplete"
    }
    if (dealerCards[0].isAce) phase = Phase.INSURANCE else startPlay()
  }

  fun placeBet(amount: Long) {
    startBetting(amount)
    dealOneToPlayer()
    dealOneToDealer()
    dealOneToPlayer()
    dealOneToDealer() // hole card
    finalizeDeal()
  }

  fun resolveInsurance(amount: Long) {
    check(phase == Phase.INSURANCE) { "Insurance not an offer" }
    require(amount in 0..hands[0].bet / 2) { "Insurance is at most half the bet" }
    require(amount <= available) { "Not enough chips" }
    insuranceBet = amount
    startPlay()
  }

  fun legalActions(): Set<Action> {
    if (phase != Phase.PLAYER_TURN) return emptySet()
    val h = hands[activeIndex]
    val twoCards = h.cards.size == 2
    return buildSet {
      add(Action.HIT)
      add(Action.STAND)
      if (twoCards && available >= h.bet) add(Action.DOUBLE)
      if (
          twoCards &&
              available >= h.bet &&
              hands.size < maxHands &&
              h.cards[0].value == h.cards[1].value
      ) {
        add(Action.SPLIT)
      }
      if (twoCards && hands.size == 1) add(Action.SURRENDER)
    }
  }

  fun act(action: Action) {
    require(action in legalActions()) { "Illegal action: $action" }
    val h = hands[activeIndex]
    when (action) {
      Action.HIT -> h.cards += shoe.deal()
      Action.STAND -> h.stood = true
      Action.SURRENDER -> h.surrendered = true
      Action.DOUBLE -> {
        h.bet *= 2
        h.isDoubled = true
        h.cards += shoe.deal()
      }
      Action.SPLIT -> {
        val moved = h.cards.removeAt(1)
        h.afterSplit = true
        h.cards += shoe.deal()
        hands += PlayerHand(mutableListOf(moved, shoe.deal()), h.bet, parentIndex = activeIndex)
      }
    }
    advance()
  }

  fun results(): List<HandResult> {
    check(phase == Phase.FINISHED)
    return hands.map {
      HandEvaluator.settle(it.cards, it.afterSplit, it.surrendered, dealerCards, mode)
    }
  }

  fun toRecord(sessionId: Long, gameNum: Int): RoundRecord {
    check(phase == Phase.FINISHED) { "Round not finished" }
    val results = results()
    val dealer = dealerValue

    val playerRecords = hands.mapIndexed { i, h ->
      HandRecord(
          hand = HandEntity.player(h.bet, h.status(), results[i], h.isDoubled),
          parentIndex = h.parentIndex,
          cards = h.cards.toHandCards(),
      )
    }
    val dealerStatus =
        when {
          dealer.isBust -> HandStatus.BUSTED
          dealer.isBlackjack -> HandStatus.BLACKJACK
          else -> HandStatus.STOOD
        }
    return RoundRecord(
        game =
            GameEntity.create(sessionId, gameNum, dealer.total, insuranceBet, dealer.isBlackjack),
        hands =
            playerRecords +
                HandRecord(
                    HandEntity.dealer(dealerStatus),
                    cards = dealerCards.toHandCards(holeIndex = 1),
                ),
    )
  }

  private fun startPlay() {
    phase = Phase.PLAYER_TURN
    if (dealerValue.isBlackjack) finish() else advance()
  }

  private fun isHandDone(h: PlayerHand): Boolean =
      h.stood ||
          h.surrendered ||
          h.isDoubled ||
          h.value.isBust ||
          h.value.isBlackjack ||
          (autoStandOn21 && h.value.total == 21)

  private fun advance() {
    while (activeIndex < hands.size && isHandDone(hands[activeIndex])) activeIndex++
    if (activeIndex >= hands.size) finish()
  }

  private fun finish() {
    if (hands.any { !it.surrendered && !it.value.isBust }) {
      while (dealerValue.total < 17) dealerCards += shoe.deal()
    }
    phase = Phase.FINISHED
  }

  private fun List<Card>.toHandCards(holeIndex: Int? = null) = mapIndexed { i, c ->
    HandCardEntity(
        handId = 0,
        dealtOrder = i,
        cardId = c.cardId,
        isHoleCard = i == holeIndex,
    )
  }
}

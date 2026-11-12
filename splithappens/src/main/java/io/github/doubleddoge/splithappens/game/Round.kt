package io.github.doubleddoge.splithappens.game

import io.github.doubleddoge.splithappens.data.dao.HandRecord
import io.github.doubleddoge.splithappens.data.dao.RoundRecord
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.enum.Action
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.game.model.HandResult
import io.github.doubleddoge.splithappens.data.enum.Phase

// One Round. Bets do NOT deduct funds on placement, Bankroll is the balance at round start.
// Reshuffles between rounds.

class Round(
	private val shoe: Shoe,
	private val mode: GameMode,
	private val bankroll: Long,
	private val minBet: Long = 0,
	private val maxHands: Int = GameRules.DEFAULT_MAX_HANDS,
	private val autoStandOn21: Boolean = true,
) {

	private val dealerUpCard get() = dealerCards[DEALER_UP_CARD_INDEX]

	private val maxInsurance get() = hands[0].bet / GameRules.INSURANCE_MAX_BET_DIVISOR
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
		check(phase == Phase.BETTING && hands.isEmpty()) { "Bet already placed" }
		require(amount > 0) { "Bet must be positive" }
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
		check(
			hands.isNotEmpty() &&
					hands[0].cards.size == GameRules.INITIAL_HAND_SIZE &&
					dealerCards.size == GameRules.INITIAL_HAND_SIZE
		) { "Initial deal is incomplete" }
		if (dealerUpCard.isAce) phase = Phase.INSURANCE else startPlay()
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
		require(amount in 0..maxInsurance) { "Insurance is at most half the bet" }
		require(amount <= available) { "Not enough chips" }
		insuranceBet = amount
		startPlay()
	}

	fun legalActions(): Set<Action> {
		if (phase != Phase.PLAYER_TURN) return emptySet()
		val hand = hands[activeIndex]
		val hasTwoCards = hand.cards.size == GameRules.INITIAL_HAND_SIZE
		val canMatchBet = available >= hand.bet
		return buildSet {
			add(Action.HIT)
			add(Action.STAND)
			if (hasTwoCards && canMatchBet) add(Action.DOUBLE)
			if (
				hasTwoCards && canMatchBet &&
				hands.size < maxHands &&
				hand.cards[0].value == hand.cards[1].value
			) {
				add(Action.SPLIT)
			}
			if (hasTwoCards && hands.size == 1) add(Action.SURRENDER)
		}
	}

	fun act(action: Action) {
		require(action in legalActions()) { "Illegal action: $action" }
		val hand = hands[activeIndex]
		when (action) {
			Action.HIT -> hand.cards += shoe.deal()
			Action.STAND -> hand.stood = true
			Action.SURRENDER -> hand.surrendered = true
			Action.DOUBLE -> {
				hand.bet *= GameRules.DOUBLE_DOWN_FACTOR
				hand.isDoubled = true
				hand.cards += shoe.deal()
			}

			Action.SPLIT -> {
				val moved = hand.cards.removeAt(hand.cards.lastIndex)
				hand.afterSplit = true
				hand.cards += shoe.deal()
				hands += PlayerHand(
					mutableListOf(moved, shoe.deal()),
					hand.bet,
					parentIndex = activeIndex
				)
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
		val dealerStatus = dealer.toHandStatus()
		return RoundRecord(
			game =
				GameEntity.create(
					sessionId,
					gameNum,
					dealer.total,
					insuranceBet,
					dealer.isBlackjack
				),
			hands =
				playerRecords +
						HandRecord(
							HandEntity.dealer(dealerStatus),
							cards = dealerCards.toHandCards(holeIndex = DEALER_HOLE_CARD_INDEX),
						),
		)
	}

	private fun startPlay() {
		phase = Phase.PLAYER_TURN
		if (dealerValue.isBlackjack) finish() else advance()
	}

	private fun isHandDone(hand: PlayerHand): Boolean {
		val value = hand.value
		return hand.stood || hand.surrendered || hand.isDoubled ||
				value.isBust || value.isBlackjack ||
				(autoStandOn21 && value.total == GameRules.BLACKJACK_TOTAL)
	}

	private fun advance() {
		while (activeIndex < hands.size && isHandDone(hands[activeIndex])) activeIndex++
		if (activeIndex >= hands.size) finish()
	}

	private fun finish() {
		val dealerMustPlay = hands.any { !it.surrendered && !it.value.isBust }
		if (dealerMustPlay) {
			while (dealerValue.total < GameRules.DEALER_STAND_TOTAL) dealerCards += shoe.deal()
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

	private companion object {
		const val DEALER_UP_CARD_INDEX = 0
		const val DEALER_HOLE_CARD_INDEX = 1
	}
}

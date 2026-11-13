package io.github.doubleddoge.splithappens.viewmodel.uistate

import io.github.doubleddoge.splithappens.game.Card
import io.github.doubleddoge.splithappens.game.PlayerHand
import io.github.doubleddoge.splithappens.game.model.Action
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.game.model.GameStage
import io.github.doubleddoge.splithappens.game.model.GameStatus
import io.github.doubleddoge.splithappens.game.model.HandResult

data class GameUiState(
	val userId: String = "",
	val displayName: String = "",
	val chipsOwned: Long = 0L,
	val currentBet: Long = 0L,
	val insuranceBet: Long = 0L,
	val gameMode: GameMode = GameMode.TRADITIONAL,
	val stage: GameStage = GameStage.BETTING,
	val playerHands: List<PlayerHand> = emptyList(),
	val activeHandIndex: Int = 0,
	val dealerCards: List<Card> = emptyList(),
	val hideHoleCard: Boolean = true,
	val legalActions: Set<Action> = emptySet(),
	val results: List<HandResult> = emptyList(),
	val statusText: String = GameStatus.PLACE_BETS,
	val hasActiveHand: Boolean = false,
) {
	val isGameInProgress: Boolean
		get() = stage == GameStage.DEALING || stage == GameStage.INSURANCE || stage == GameStage.PLAYER_TURN
}
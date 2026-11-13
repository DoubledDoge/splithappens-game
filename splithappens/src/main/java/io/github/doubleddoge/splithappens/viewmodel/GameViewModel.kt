package io.github.doubleddoge.splithappens.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.RoundDao
import io.github.doubleddoge.splithappens.data.dao.SessionDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import io.github.doubleddoge.splithappens.game.model.Action
import io.github.doubleddoge.splithappens.game.GameSessionManager
import io.github.doubleddoge.splithappens.game.model.Phase
import io.github.doubleddoge.splithappens.data.pref.PrefFiles
import io.github.doubleddoge.splithappens.data.pref.PrefKeys
import io.github.doubleddoge.splithappens.game.Round
import io.github.doubleddoge.splithappens.game.Shoe
import io.github.doubleddoge.splithappens.game.model.BALANCE_THRESHOLD
import io.github.doubleddoge.splithappens.game.model.GameStage
import io.github.doubleddoge.splithappens.game.model.GameStatus
import io.github.doubleddoge.splithappens.viewmodel.uistate.GameUiState
import io.github.doubleddoge.splithappens.game.outcomeMessage
import io.github.doubleddoge.splithappens.game.snapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val DEAL_DELAY = 350.milliseconds
private val FAST_DEAL_DELAY = 100.milliseconds
private val DEALER_TURN_DELAY = 600.milliseconds
private val FAST_DEALER_TURN_DELAY = 180.milliseconds

private const val SHOE_DECK_COUNT = 6

class GameViewModel(
	application: Application,
	private val userDao: UserDao,
	private val sessionDao: SessionDao,
	private val roundDao: RoundDao,
	private val targetUserId: String = "",
	initialMode: GameMode = GameMode.TRADITIONAL,
) : ViewModel() {

	private val prefs = application.getSharedPreferences(PrefFiles.APP, Context.MODE_PRIVATE)

	private val isFastDeal: Boolean
		get() = prefs.getBoolean(PrefKeys.FAST_DEAL, false)

	private val isAutoStandOn21: Boolean
		get() = prefs.getBoolean(PrefKeys.AUTO_STAND_ON_21, true)

	private val _uiState = MutableStateFlow(GameUiState(gameMode = initialMode))
	val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

	private val shoe = Shoe(deckCount = SHOE_DECK_COUNT)
	private var roundJob: Job? = null

	private var currentRound: Round? = null
	private var sessionManager: GameSessionManager? = null
	private var currentUserId: String = ""

	init {
		observeUserData()
	}

	private fun observeUserData() {
		viewModelScope.launch {
			val userFlow =
				if (targetUserId.isNotBlank()) {
					userDao.observeById(targetUserId)
				} else {
					userDao.observeAll().map { users -> users.firstOrNull() }
				}

			userFlow.collect { user ->
				if (user != null) {
					if ((sessionManager == null) || (currentUserId != user.userId)) {
						sessionManager?.endSession(SessionStatus.COMPLETED)
						currentUserId = user.userId
						startNewSession(user.userId, _uiState.value.gameMode)
					}

					_uiState.update { current ->
						current.copy(
							userId = user.userId,
							displayName = user.displayName,
							chipsOwned = user.chipsOwned,
						)
					}
				}
			}
		}
	}

	fun setGameMode(mode: GameMode) {
		_uiState.update { it.copy(gameMode = mode) }
		if (_uiState.value.stage == GameStage.BETTING) {
			val userId = _uiState.value.userId
			if (userId.isNotBlank()) {
				viewModelScope.launch {
					startNewSession(userId, mode)
				}
			}
		}
	}

	fun addBet(amount: Long) {
		val uiState = _uiState.value
		if (uiState.stage != GameStage.BETTING) return
		val available = uiState.chipsOwned - uiState.currentBet
		if (available < amount) {
			_uiState.update { it.copy(statusText = GameStatus.NOT_ENOUGH_CHIPS) }
			return
		}

		val newBet = uiState.currentBet + amount
		_uiState.update {
			it.copy(
				currentBet = newBet,
				statusText = "Bet: R$newBet",
			)
		}
	}

	fun clearBet() {
		val uiState = _uiState.value
		if (uiState.stage != GameStage.BETTING || uiState.currentBet == 0L) return
		_uiState.update {
			it.copy(
				currentBet = 0L,
				statusText = GameStatus.PLACE_BETS,
			)
		}
	}

	fun deal() {
		val uiState = _uiState.value

		if (uiState.stage != GameStage.BETTING) return

		if (uiState.chipsOwned <= BALANCE_THRESHOLD) {
			_uiState.update {
				it.copy(statusText = GameStatus.LOW_BALANCE)
			}
			return
		}

		if (uiState.currentBet <= 0L) {
			_uiState.update { it.copy(statusText = GameStatus.PLACE_A_BET) }
			return
		}

		if (shoe.needsReshuffle) shoe.reshuffle()

		val round =
			Round(
				shoe = shoe,
				mode = uiState.gameMode,
				bankroll = uiState.chipsOwned,
				minBet = 0L,
				autoStandOn21 = isAutoStandOn21,
			)
		currentRound = round
		round.startBetting(uiState.currentBet)

		_uiState.update {
			it.copy(
				insuranceBet = 0L,
				stage = GameStage.DEALING,
				statusText = GameStatus.DEALING,
				playerHands = round.hands.snapshot(),
				activeHandIndex = 0,
				dealerCards = emptyList(),
				hideHoleCard = true,
				hasActiveHand = true,
			)
		}
		roundJob = viewModelScope.launch {
			val dealDelay = if (isFastDeal) FAST_DEAL_DELAY else DEAL_DELAY
			val dealSteps = listOf(
				round::dealOneToPlayer, round::dealOneToDealer,
				round::dealOneToPlayer, round::dealOneToDealer,
			)
			for (dealStep in dealSteps) {
				dealStep()
				updateDealState(round)
				delay(dealDelay)
			}
			round.finalizeDeal()

			_uiState.update {
				it.copy(
					stage =
						if (round.phase == Phase.INSURANCE) GameStage.INSURANCE else GameStage.PLAYER_TURN,
					legalActions = round.legalActions(),
					statusText =
						if (round.phase == Phase.INSURANCE) GameStatus.INSURANCE_OFFERED else GameStatus.HIT_OR_STAND,
				)
			}

			if (round.phase == Phase.FINISHED) finishRound()
		}
	}

	private fun updateDealState(round: Round) {
		_uiState.update {
			it.copy(
				playerHands = round.hands.snapshot(),
				dealerCards = round.dealerCards.toList(),
				hideHoleCard = round.holeHidden,
			)
		}
	}

	fun performAction(action: Action) {
		val round = currentRound ?: return
		if (round.phase != Phase.PLAYER_TURN) return

		round.act(action)

		val totalStaked = round.hands.sumOf { it.bet }
		val statusPrompt =
			if (round.hands.size > 1 && round.phase == Phase.PLAYER_TURN) {
				"Playing Hand ${round.activeIndex + 1} of ${round.hands.size}"
			} else {
				_uiState.value.statusText
			}

		_uiState.update {
			it.copy(
				currentBet = totalStaked,
				playerHands = round.hands.snapshot(),
				activeHandIndex = round.activeIndex,
				dealerCards = round.dealerCards.toList(),
				hideHoleCard = round.holeHidden,
				legalActions = round.legalActions(),
				statusText = statusPrompt,
			)
		}

		if (round.phase == Phase.FINISHED) {
			roundJob = viewModelScope.launch {
				_uiState.update {
					it.copy(
						stage = GameStage.DEALER_TURN,
						statusText = GameStatus.DEALER_DRAWING,
					)
				}
				val turnDelay = if (isFastDeal) FAST_DEALER_TURN_DELAY else DEALER_TURN_DELAY
				delay(turnDelay)
				finishRound()
			}
		}
	}

	fun resolveInsurance(accept: Boolean) {
		val round = currentRound ?: return
		if (round.phase != Phase.INSURANCE) return

		val wanted = if (accept) round.maxInsurance else 0L
		val cantAfford = !round.canAffordInsurance(wanted)
		val amount = if (cantAfford) 0L else wanted
		round.resolveInsurance(amount)

		_uiState.update {
			it.copy(
				insuranceBet = amount,
				stage = if (round.phase == Phase.FINISHED) GameStage.ROUND_OVER else GameStage.PLAYER_TURN,
				playerHands = round.hands.snapshot(),
				dealerCards = round.dealerCards.toList(),
				hideHoleCard = round.holeHidden,
				legalActions = round.legalActions(),
				statusText = when {
					cantAfford -> GameStatus.INSURANCE_UNAFFORDABLE
					round.phase == Phase.FINISHED -> it.statusText
					else -> GameStatus.HIT_OR_STAND
				},
			)
		}
		if (round.phase == Phase.FINISHED) finishRound()
	}

	private suspend fun startNewSession(userId: String, mode: GameMode) {
		sessionManager?.endSession(SessionStatus.COMPLETED)
		sessionManager = GameSessionManager(sessionDao, roundDao, userId, BALANCE_THRESHOLD, mode)
			.also { it.startSession() }
	}

	private fun finishRound() {
		val round = currentRound ?: return
		val results = round.results()
		val outcomeMessage = outcomeMessage(results, round.hands.firstOrNull()?.value?.isBust == true)

		_uiState.update {
			it.copy(
				stage = GameStage.ROUND_OVER,
				playerHands = round.hands.snapshot(),
				dealerCards = round.dealerCards.toList(),
				hideHoleCard = false,
				results = results,
				legalActions = emptySet(),
				statusText = outcomeMessage,
				hasActiveHand = false,
				currentBet = 0L,
				insuranceBet = 0L,
			)
		}

		viewModelScope.launch {
			val sm = sessionManager
			if (sm != null) {
				if (!sm.isSessionActive) {
					sm.startSession()
				}
				sm.saveAndNextRound(round)
			}
		}
	}

	fun forfeitRound() {
		currentRound = null
		_uiState.update {
			it.copy(
				currentBet = 0L,
				insuranceBet = 0L,
				activeHandIndex = 0,
				playerHands = emptyList(),
				dealerCards = emptyList(),
				hideHoleCard = true,
				stage = GameStage.BETTING,
				results = emptyList(),
				statusText = GameStatus.PLACE_BETS,
				hasActiveHand = false,
			)
		}
		roundJob?.cancel()
	}

	override fun onCleared() {
		viewModelScope.launch { sessionManager?.endSession(SessionStatus.COMPLETED) }
	}
}



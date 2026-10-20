package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.RoundDao
import io.github.doubleddoge.splithappens.data.dao.SessionDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import io.github.doubleddoge.splithappens.game.Action
import io.github.doubleddoge.splithappens.game.Card
import io.github.doubleddoge.splithappens.game.GameSessionManager
import io.github.doubleddoge.splithappens.game.Phase
import io.github.doubleddoge.splithappens.game.PlayerHand
import io.github.doubleddoge.splithappens.game.Round
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

enum class DealerEmotion { NEUTRAL, SMUG, BUSTED }

enum class GameStage { BETTING, DEALING, INSURANCE, PLAYER_TURN, DEALER_TURN, ROUND_OVER }

data class GameUiState(
    val userId: String = "",
    val displayName: String = "",
    val chipsOwned: Long = 0L,
    val currentBet: Long = 0L,
    val stage: GameStage = GameStage.BETTING,
    val playerHands: List<PlayerHand> = emptyList(),
    val activeHandIndex: Int = 0,
    val dealerCards: List<Card> = emptyList(),
    val hideHoleCard: Boolean = true,
    val legalActions: Set<Action> = emptySet(),
    val results: List<HandResult> = emptyList(),
    val statusText: String = "Place your bets"
)

class GameViewModel(
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val roundDao: RoundDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val shoe = io.github.doubleddoge.splithappens.game.Shoe(deckCount = 6)
    private var roundJob: Job? = null

    private var currentRound: Round? = null
    private var sessionManager: GameSessionManager? = null
    private var currentUserId: String = ""

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userDao.observeAll().collect { users ->
                val user = users.firstOrNull()
                if (user != null) {
                    if (sessionManager == null || currentUserId != user.userId) {
                        sessionManager?.endSession(SessionStatus.COMPLETED)
                        currentUserId = user.userId
                        sessionManager = GameSessionManager(
                            sessionDao = sessionDao,
                            roundDao = roundDao,
                            userId = user.userId,
                            stakes = 100L,
                            mode = GameMode.TRADITIONAL
                        ).apply {
                            startSession()
                        }
                    }

                    _uiState.update { current ->
                        current.copy(
                            userId = user.userId,
                            displayName = user.displayName,
                            chipsOwned = user.chipsOwned
                        )
                    }
                }
            }
        }
    }

    // --- BETTING ---

    fun addBet(amount: Long) {
        val s = _uiState.value
        if (s.stage != GameStage.BETTING || (s.chipsOwned - s.currentBet) < amount) return

        val newBet = s.currentBet + amount
        _uiState.update {
            it.copy(
                currentBet = newBet,
                statusText = "Bet: R$newBet"
            )
        }
    }

    fun clearBet() {
        val s = _uiState.value
        if (s.stage != GameStage.BETTING || s.currentBet == 0L) return
        _uiState.update {
            it.copy(
                currentBet = 0L,
                statusText = "Place your bets"
            )
        }
    }

    // --- DEAL ---

    fun deal() {
        val s = _uiState.value
        if (s.stage != GameStage.BETTING || s.currentBet <= 0L) return

        if (shoe.needsReshuffle) shoe.reshuffle()

        val round = Round(
            shoe = shoe,
            mode = GameMode.TRADITIONAL,
            bankroll = s.chipsOwned,
            minBet = 5L
        )
        currentRound = round
        round.startBetting(s.currentBet)

        _uiState.update {
            it.copy(
                stage = GameStage.DEALING,
                statusText = "Dealing...",
                playerHands = round.hands.toList(),
                dealerCards = emptyList(),
                hideHoleCard = true
            )
        }
        roundJob = viewModelScope.launch {
            // 1. Card to Player
            round.dealOneToPlayer()
            updateDealState(round)
            delay(350.milliseconds)

            // 2. Up-card to Dealer
            round.dealOneToDealer()
            updateDealState(round)
            delay(350.milliseconds)

            // 3. Card to Player
            round.dealOneToPlayer()
            updateDealState(round)
            delay(350.milliseconds)

            // 4. Hole-card to Dealer
            round.dealOneToDealer()
            updateDealState(round)
            delay(350.milliseconds)

            round.finalizeDeal()

            _uiState.update {
                it.copy(
                    stage = if (round.phase == Phase.INSURANCE) GameStage.INSURANCE else GameStage.PLAYER_TURN,
                    legalActions = round.legalActions(),
                    statusText = if (round.phase == Phase.INSURANCE) "Insurance Offered" else "Hit or Stand?"
                )
            }

            if (round.phase == Phase.FINISHED) finishRound()
        }
    }

    private fun updateDealState(round: Round) {
        _uiState.update {
            it.copy(
                playerHands = round.hands.toList(),
                dealerCards = round.dealerCards.toList(),
                hideHoleCard = round.holeHidden
            )
        }
    }

    fun performAction(action: Action) {
        val round = currentRound ?: return
        if (round.phase != Phase.PLAYER_TURN) return

        round.act(action)
        _uiState.update {
            it.copy(
                playerHands = round.hands.toList(),
                activeHandIndex = round.activeIndex,
                dealerCards = round.dealerCards.toList(),
                hideHoleCard = round.holeHidden,
                legalActions = round.legalActions()
            )
        }
        if (round.phase == Phase.FINISHED) {
            roundJob = viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        stage = GameStage.DEALER_TURN,
                        statusText = "Dealer drawing..."
                    )
                }
                delay(600.milliseconds)
                finishRound()
            }
        }
    }

    fun resolveInsurance(accept: Boolean) {
        val round = currentRound ?: return
        if (round.phase != Phase.INSURANCE) return

        val amount = if (accept) _uiState.value.currentBet / 2 else 0L
        round.resolveInsurance(amount)

        _uiState.update {
            it.copy(
                stage = if (round.phase == Phase.FINISHED) GameStage.ROUND_OVER else GameStage.PLAYER_TURN,
                playerHands = round.hands.toList(),
                dealerCards = round.dealerCards.toList(),
                hideHoleCard = round.holeHidden,
                legalActions = round.legalActions()
            )
        }

        if (round.phase == Phase.FINISHED) finishRound()
    }

    private fun finishRound() {
        val round = currentRound ?: return
        val results = round.results()

        // Map the result of the primary hand to a banner message
        val primaryResult = results.firstOrNull()
        val outcomeMessage = when (primaryResult) {
            HandResult.BLACKJACK, HandResult.NATURAL_BLACKJACK, HandResult.QUEEN_JACK -> "BLACKJACK! YOU WIN!"
            HandResult.WIN, HandResult.TRIPLE_SEVEN, HandResult.SIX_CARD, HandResult.SEVEN_CARD -> "YOU WIN!"
            HandResult.PUSH -> "PUSH - BET RETURNED"
            HandResult.LOSS -> if (round.hands.firstOrNull()?.value?.isBust == true) "BUSTED!" else "DEALER WINS"
            HandResult.SURRENDER -> "SURRENDERED"
            null -> "ROUND OVER"
        }

        _uiState.update {
            it.copy(
                stage = GameStage.ROUND_OVER,
                hideHoleCard = false,
                results = results,
                legalActions = emptySet(),
                statusText = outcomeMessage
            )
        }

        viewModelScope.launch {
            val sm = sessionManager
            if (sm != null) {
                if (!sm.isSessionActive) {
                    sm.startSession()
                }
                sm.saveAndNextRound(round, _uiState.value.chipsOwned)
            }
        }
    }

    fun resetForNextRound() {
        currentRound = null
        _uiState.update {
            it.copy(
                currentBet = 0L,
                playerHands = emptyList(),
                dealerCards = emptyList(),
                hideHoleCard = true,
                stage = GameStage.BETTING,
                results = emptyList(),
                statusText = "Place your bets"
            )
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            sessionManager?.endSession(SessionStatus.COMPLETED)
        }
    }
}

class GameViewModelFactory(
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val roundDao: RoundDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(userDao, sessionDao, roundDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

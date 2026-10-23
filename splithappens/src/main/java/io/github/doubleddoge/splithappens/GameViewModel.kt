package io.github.doubleddoge.splithappens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
private val DEALER_TURN_DELAY = 600.milliseconds

enum class DealerEmotion {
  NEUTRAL,
  SMUG,
  BUSTED,
}

enum class GameStage {
  BETTING,
  DEALING,
  INSURANCE,
  PLAYER_TURN,
  DEALER_TURN,
  ROUND_OVER,
}

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
    val statusText: String = "Place your bets",
    val hasActiveHand: Boolean = false,
) {
  val isGameInProgress: Boolean
    get() = stage != GameStage.BETTING || currentBet > 0L || hasActiveHand
}

class GameViewModel(
    application: Application,
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val roundDao: RoundDao,
    private val targetUserId: String = "",
    initialMode: GameMode = GameMode.TRADITIONAL,
) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(
      GameUiState(gameMode = initialMode)
  )
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
      val userFlow = if (targetUserId.isNotBlank()) {
        userDao.observeById(targetUserId)
      } else {
        userDao.observeAll().map { users -> users.firstOrNull() }
      }

      userFlow.collect { user ->
        if (user != null) {
          if (sessionManager == null || currentUserId != user.userId) {
            sessionManager?.endSession(SessionStatus.COMPLETED)
            currentUserId = user.userId
            sessionManager =
                GameSessionManager(
                    sessionDao = sessionDao,
                    roundDao = roundDao,
                    userId = user.userId,
                    stakes = 100L,
                    mode = _uiState.value.gameMode,
                )
                    .apply { startSession() }
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
          sessionManager?.endSession(SessionStatus.COMPLETED)
          sessionManager =
              GameSessionManager(
                  sessionDao = sessionDao,
                  roundDao = roundDao,
                  userId = userId,
                  stakes = 100L,
                  mode = mode,
              )
                  .apply { startSession() }
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
          statusText = "Bet: R$newBet",
      )
    }
  }

  fun clearBet() {
    val s = _uiState.value
    if (s.stage != GameStage.BETTING || s.currentBet == 0L) return
    _uiState.update {
      it.copy(
          currentBet = 0L,
          statusText = "Place your bets",
      )
    }
  }

  // --- DEAL ---

  fun deal() {
    val s = _uiState.value
    if (s.stage != GameStage.BETTING) return
    if (s.currentBet < 100L) {
      _uiState.update { it.copy(statusText = "Minimum bet is R100") }
      return
    }

    if (shoe.needsReshuffle) shoe.reshuffle()

    val round =
        Round(
            shoe = shoe,
            mode = s.gameMode,
            bankroll = s.chipsOwned,
            minBet = 100L,
        )
    currentRound = round
    round.startBetting(s.currentBet)

    _uiState.update {
      it.copy(
          insuranceBet = 0L,
          stage = GameStage.DEALING,
          statusText = "Dealing...",
          playerHands = round.hands.snapshot(),
          dealerCards = emptyList(),
          hideHoleCard = true,
          hasActiveHand = true,
      )
    }
    roundJob =
        viewModelScope.launch {
          round.dealOneToPlayer()
          updateDealState(round)
          delay(DEAL_DELAY)

          round.dealOneToDealer()
          updateDealState(round)
          delay(DEAL_DELAY)

          round.dealOneToPlayer()
          updateDealState(round)
          delay(DEAL_DELAY)

          round.dealOneToDealer()
          updateDealState(round)
          delay(DEAL_DELAY)

          round.finalizeDeal()

          _uiState.update {
            it.copy(
                stage =
                    if (round.phase == Phase.INSURANCE) GameStage.INSURANCE
                    else GameStage.PLAYER_TURN,
                legalActions = round.legalActions(),
                statusText =
                    if (round.phase == Phase.INSURANCE) "Insurance Offered" else "Hit or Stand?",
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
    _uiState.update {
      it.copy(
          playerHands = round.hands.snapshot(),
          activeHandIndex = round.activeIndex,
          dealerCards = round.dealerCards.toList(),
          hideHoleCard = round.holeHidden,
          legalActions = round.legalActions(),
      )
    }
    if (round.phase == Phase.FINISHED) {
      roundJob =
          viewModelScope.launch {
            _uiState.update {
              it.copy(
                  stage = GameStage.DEALER_TURN,
                  statusText = "Dealer drawing...",
              )
            }
            delay(DEALER_TURN_DELAY)
            finishRound()
          }
    }
  }

  fun resolveInsurance(accept: Boolean) {
    val round = currentRound ?: return
    if (round.phase != Phase.INSURANCE) return

    val maxInsurance = _uiState.value.currentBet / 2
    val amount = if (accept) maxInsurance else 0L
    round.resolveInsurance(amount)

    _uiState.update {
      it.copy(
          insuranceBet = amount,
          stage =
              if (round.phase == Phase.FINISHED) GameStage.ROUND_OVER else GameStage.PLAYER_TURN,
          playerHands = round.hands.snapshot(),
          dealerCards = round.dealerCards.toList(),
          hideHoleCard = round.holeHidden,
          legalActions = round.legalActions(),
          statusText = if (round.phase == Phase.FINISHED) it.statusText else "Hit or Stand?",
      )
    }

    if (round.phase == Phase.FINISHED) finishRound()
  }

  private fun finishRound() {
    val round = currentRound ?: return
    val results = round.results()
    val primaryResult = results.firstOrNull()
    val outcomeMessage = outcomeBanner(round, primaryResult)

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
        sm.saveAndNextRound(round, _uiState.value.chipsOwned)
      }
    }
  }

  fun forfeitRound() {
    currentRound = null
    _uiState.update {
      it.copy(
          currentBet = 0L,
          insuranceBet = 0L,
          playerHands = emptyList(),
          dealerCards = emptyList(),
          hideHoleCard = true,
          stage = GameStage.BETTING,
          results = emptyList(),
          statusText = "Place your bets",
          hasActiveHand = false,
      )
    }
  }

  fun resetForNextRound() {
    currentRound = null
    _uiState.update {
      it.copy(
          currentBet = 0L,
          insuranceBet = 0L,
          playerHands = emptyList(),
          dealerCards = emptyList(),
          hideHoleCard = true,
          stage = GameStage.BETTING,
          results = emptyList(),
          statusText = "Place your bets",
          hasActiveHand = false,
      )
    }
  }

  private fun outcomeBanner(round: Round, primary: HandResult?): String =
      when (primary) {
        HandResult.BLACKJACK,
        HandResult.NATURAL_BLACKJACK,
        HandResult.QUEEN_JACK -> "BLACKJACK! YOU WIN!"
        HandResult.WIN,
        HandResult.TRIPLE_SEVEN,
        HandResult.SIX_CARD,
        HandResult.SEVEN_CARD -> "YOU WIN!"
        HandResult.PUSH -> "PUSH - BET RETURNED"
        HandResult.LOSS ->
            if (round.hands.firstOrNull()?.value?.isBust == true) "BUSTED!" else "DEALER WINS"
        HandResult.SURRENDER -> "SURRENDERED"
        null -> "ROUND OVER"
      }

  override fun onCleared() {
    viewModelScope.launch { sessionManager?.endSession(SessionStatus.COMPLETED) }
  }
}

class GameViewModelFactory(
    private val application: Application,
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val roundDao: RoundDao,
    private val targetUserId: String = "",
    private val mode: GameMode = GameMode.TRADITIONAL,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
      return GameViewModel(application, userDao, sessionDao, roundDao, targetUserId, mode) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
  }
}

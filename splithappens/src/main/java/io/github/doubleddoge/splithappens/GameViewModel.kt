package io.github.doubleddoge.splithappens

import android.app.ProgressDialog.show
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.enum.GameMode
import io.github.doubleddoge.splithappens.data.enum.HandResult
import io.github.doubleddoge.splithappens.game.Action
import io.github.doubleddoge.splithappens.game.Card
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

enum class GameStage { BETTING, INSURANCE, PLAYER_TURN, DEALER_TURN, ROUND_OVER }

enum class RoundResult(val bannerMessage: String, val handResult: HandResult?) {
    NONE("", null),
    PLAYER_BLACKJACK("BLACKJACK! PAYS 3:2", HandResult.BLACKJACK),
    PLAYER_WIN("YOU WIN!", HandResult.WIN),
    DEALER_BUST("DEALER BUSTS! YOU WIN", HandResult.WIN),
    PUSH("PUSH - BET RETURNED", HandResult.PUSH),
    DEALER_WIN("DEALER WINS", HandResult.LOSS),
    DEALER_BLACKJACK("DEALER HAS BLACKJACK", HandResult.LOSS),
    PLAYER_BUST("BUSTED!", HandResult.LOSS)
}

data class GameUiState(
    val userId: String = "default_player",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500L,
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
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val shoe = Shoe(deckCount = 6)
    private var roundJob: Job? = null

    private var currentRound: Round? = null

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userDao.observeAll().collect { users ->
                val user = users.firstOrNull()
                if (user != null) {
                    _uiState.update { current ->
                        val idle = current.stage == GameStage.BETTING && current.currentBet == 0L
                        current.copy(
                            userId = user.userId,
                            displayName = user.displayName,
                            chipsOwned = if (idle) user.chipsOwned else current.chipsOwned
                        )
                    }
                }
            }
        }
    }

    // --- BETTING ---

    fun addBet(amount: Long) {
        val s = _uiState.value
        if (s.stage != GameStage.BETTING || s.chipsOwned < amount) return


        val newBet = s.currentBet + amount
        _uiState.update {
            it.copy(
                chipsOwned = it.chipsOwned - amount,
                currentBet = newBet,
                statusText ="Bet: R$newBet"
            )
        }
    }

    fun clearBet() {
        val s = _uiState.value
        if (s.stage != GameStage.BETTING || s.currentBet == 0L) return
        _uiState.update {
            it.copy(
                chipsOwned = it.chipsOwned + it.currentBet,
                currentBet = 0L,
                statusText =  "Place your bets"
            )
        }
    }

    // --- DEAL ---

    fun deal() {
        val s = _uiState.value
        if (s.stage != GameStage.BETTING || s.currentBet <= 0L) return

        if (shoe.needsReshuffle) show.reshuffle()
        persistUserChips(s.chipsOwned)
        val round = Round(
            shoe = shoe,
            mode = GameMode.STANDARD,
            bankroll = s.chipsOwned + s.currentBet,
            minBet = 5L
        )
        _uiState.update {
            it.copy(
                stage = if (round.phase == Phase.INSURANCE) GameStage.INSURANCE else GameStage.PLAYER_TURN,
                playerHand = round.hands.toList(),
                activeHandIndex = round.activeIndex,
                dealerCards = round.dealerCards.toList(),
                hideholeCard = round.holeHidden,
                legalActions = round.legalActions(),
                statusText = if (round.phase == Phase.INSURANCE) "Insurance Offered" else "Your turn"
            )
        }
        if(round.phase == Phase.FINISHED) finishRound()
    }

    fun performAction(action: Action){
        val round = currentRound?: return
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
        if(round.phase == Phase.FINISHED){
            roundJob = viewModelScope.launch {
                _uiState.update { it.copy(stage = GameStage.DEALER_TURN, statusText = "Dealer drawing..."   ) }
                delay(600.milliseconds)
                finishRound()
            }
        }
    }

    private fun reso(toPlayer: Boolean, faceUp: Boolean = true) {
        val card = shoe.draw(faceUp)
        _uiState.update { s ->
            if (toPlayer) {
                val hand = s.playerHand + card
                val (value, soft) = calculateHandValue(hand)
                s.copy(playerHand = hand, playerHandValue = value, isPlayerSoft = soft)
            } else {
                val hand = s.dealerHand + card
                s.copy(dealerHand = hand, dealerHandValue = calculateHandValue(hand).first)
            }
        }
    }

    private fun revealHoleCard() {
        _uiState.update { s ->
            val hand = s.dealerHand.map { it.copy(isFaceUp = true) }
            val value = calculateHandValue(hand).first
            s.copy(dealerHand = hand, dealerHandValue = value, dealerStatusText = "Dealer has $value")
        }
    }

    // --- PLAYER ACTIONS ---

    fun hit() {
        if (_uiState.value.stage != GameStage.PLAYER_TURN) return
        dealCard(toPlayer = true)
        val value = _uiState.value.playerHandValue
        when {
            value > 21 -> endRound(RoundResult.PLAYER_BUST, DealerEmotion.SMUG, "Busted with $value!")
            value == 21 -> stand()
            else -> _uiState.update {
                it.copy(canDoubleDown = false, canSplit = false, dealerStatusText = "Player has $value")
            }
        }
    }

    fun stand() {
        if (_uiState.value.stage != GameStage.PLAYER_TURN) return
        _uiState.update { it.copy(stage = GameStage.DEALER_TURN, canDoubleDown = false, canSplit = false) }
        roundJob = viewModelScope.launch { playOutDealer() }
    }

    fun doubleDown() {
        val s = _uiState.value
        if (s.stage != GameStage.PLAYER_TURN || !s.canDoubleDown) return

        val newChips = s.chipsOwned - s.currentBet
        persistUserChips(newChips)
        _uiState.update {
            it.copy(
                chipsOwned = newChips,
                currentBet = it.currentBet * 2,
                isDoubled = true,
                canDoubleDown = false,
                canSplit = false
            )
        }
        dealCard(toPlayer = true)

        if (_uiState.value.playerHandValue > 21) {
            endRound(RoundResult.PLAYER_BUST, DealerEmotion.SMUG, "Double Down Bust!")
        } else {
            stand()
        }
    }

    // --- DEALER ---

    private suspend fun playOutDealer() {
        delay(500.milliseconds)
        revealHoleCard()
        delay(700.milliseconds)

        while (_uiState.value.dealerHandValue < 17) {
            dealCard(toPlayer = false)
            _uiState.update { it.copy(dealerStatusText = "Dealer draws: ${it.dealerHandValue}") }
            delay(800.milliseconds)
        }

        val p = _uiState.value.playerHandValue
        val d = _uiState.value.dealerHandValue
        when {
            d > 21 -> endRound(RoundResult.DEALER_BUST, DealerEmotion.BUSTED, "Dealer busts!")
            p > d -> endRound(RoundResult.PLAYER_WIN, DealerEmotion.BUSTED, "Player wins $p vs $d")
            d > p -> endRound(RoundResult.DEALER_WIN, DealerEmotion.SMUG, "Dealer wins $d vs $p")
            else -> endRound(RoundResult.PUSH, DealerEmotion.NEUTRAL, "Push on $p")
        }
    }

    // --- PAYOUT & PERSISTENCE ---

    private fun endRound(result: RoundResult, emotion: DealerEmotion, statusText: String) {
        val s = _uiState.value
        val payout = result.handResult?.payoutFor(s.currentBet) ?: 0L
        val finalChips = s.chipsOwned + payout
        val revealed = s.dealerHand.map { it.copy(isFaceUp = true) }

        _uiState.update {
            it.copy(
                chipsOwned = finalChips,
                dealerHand = revealed,
                dealerHandValue = calculateHandValue(revealed).first,
                stage = GameStage.ROUND_OVER,
                result = result,
                dealerEmotion = emotion,
                dealerStatusText = statusText,
                canDoubleDown = false,
                canSplit = false
            )
        }
        persistUserChips(finalChips)
    }

    private fun persistUserChips(newChips: Long) {
        val userId = _uiState.value.userId
        if (userId.isNotEmpty()) {
            viewModelScope.launch { userDao.setChips(userId, newChips) }
        }
    }

    fun resetForNextRound() {
        if (_uiState.value.stage != GameStage.ROUND_OVER) return
        _uiState.update {
            it.copy(
                currentBet = 0L,
                isDoubled = false,
                playerHand = emptyList(),
                dealerHand = emptyList(),
                playerHandValue = 0,
                dealerHandValue = 0,
                isPlayerSoft = false,
                stage = GameStage.BETTING,
                result = RoundResult.NONE,
                dealerEmotion = DealerEmotion.NEUTRAL,
                dealerStatusText = "Place your bets",
                canDoubleDown = false,
                canSplit = false
            )
        }
    }

    private companion object {
        val DEAL_DELAY = 350.milliseconds
    }
}

class GameViewModelFactory(
    private val userDao: UserDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
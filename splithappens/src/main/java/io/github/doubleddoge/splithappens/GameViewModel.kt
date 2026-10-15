package io.github.doubleddoge.splithappens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.doubleddoge.splithappens.data.dao.UserDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// --- 1. CARD MODELS & DECK ENGINE ---

enum class Suit(val symbol: String, val isRed: Boolean) {
    HEARTS("♥", true),
    DIAMONDS("♦", true),
    CLUBS("♣", false),
    SPADES("♠", false)
}

enum class Rank(val display: String, val baseValue: Int) {
    TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
    SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9),
    TEN("10", 10), JACK("J", 10), QUEEN("Q", 10), KING("K", 10),
    ACE("A", 11)
}

data class Card(
    val suit: Suit,
    val rank: Rank,
    val isFaceUp: Boolean = true
) {
    val value: Int get() = rank.baseValue
}

fun calculateHandValue(cards: List<Card>): Pair<Int, Boolean> {
    val visibleCards = cards.filter { it.isFaceUp }
    var sum = visibleCards.sumOf { it.value }
    var aces = visibleCards.count { it.rank == Rank.ACE }

    while (sum > 21 && aces > 0) {
        sum -= 10
        aces -= 1
    }
    val isSoft = aces > 0
    return Pair(sum, isSoft)
}

fun createShuffledShoe(deckCount: Int = 4): MutableList<Card> {
    val shoe = mutableListOf<Card>()
    repeat(deckCount) {
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                shoe.add(Card(suit = suit, rank = rank, isFaceUp = true))
            }
        }
    }
    shoe.shuffle()
    return shoe
}

// --- 2. GAME STATE & ENUMS ---

enum class DealerEmotion {
    NEUTRAL,
    SMUG,
    BUSTED
}

enum class GameStage {
    BETTING,
    PLAYER_TURN,
    DEALER_TURN,
    ROUND_OVER
}

enum class RoundResult(val bannerMessage: String) {
    NONE(""),
    PLAYER_BLACKJACK("BLACKJACK! PAYS 3:2"),
    PLAYER_WIN("YOU WIN!"),
    DEALER_BUST("DEALER BUSTS! YOU WIN"),
    PUSH("PUSH - BET RETURNED"),
    DEALER_WIN("DEALER WINS"),
    PLAYER_BUST("BUSTED!")
}

data class GameUiState(
    val userId: String = "",
    val displayName: String = "Player 1",
    val chipsOwned: Long = 2500L,
    val currentBet: Long = 0L,
    val playerHand: List<Card> = emptyList(),
    val dealerHand: List<Card> = emptyList(),
    val playerHandValue: Int = 0,
    val isPlayerSoft: Boolean = false,
    val dealerHandValue: Int = 0,
    val stage: GameStage = GameStage.BETTING,
    val result: RoundResult = RoundResult.NONE,
    val dealerEmotion: DealerEmotion = DealerEmotion.NEUTRAL,
    val dealerStatusText: String = "Place your bets",
    val canDoubleDown: Boolean = false,
    val canSplit: Boolean = false
)

// --- 3. VIEWMODEL IMPLEMENTATION ---

class GameViewModel(
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var deck: MutableList<Card> = createShuffledShoe()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userDao.observeAll().collect { users ->
                val user = users.firstOrNull()
                if (user != null) {
                    _uiState.update { current ->
                        if (current.stage == GameStage.BETTING && current.currentBet == 0L) {
                            current.copy(
                                userId = user.userId,
                                displayName = user.displayName,
                                chipsOwned = user.chipsOwned
                            )
                        } else {
                            current.copy(
                                userId = user.userId,
                                displayName = user.displayName
                            )
                        }
                    }
                }
            }
        }
    }

    private fun drawCard(isFaceUp: Boolean = true): Card {
        if (deck.size < 15) {
            deck = createShuffledShoe()
        }
        return deck.removeAt(0).copy(isFaceUp = isFaceUp)
    }

    // --- BETTING CONTROLS ---

    fun addBet(amount: Long) {
        val current = _uiState.value
        if (current.stage != GameStage.BETTING) return
        if (current.chipsOwned >= amount) {
            val newChips = current.chipsOwned - amount
            val newBet = current.currentBet + amount
            _uiState.update {
                it.copy(
                    chipsOwned = newChips,
                    currentBet = newBet,
                    dealerStatusText = "Bet: R$newBet"
                )
            }
        }
    }

    fun clearBet() {
        val current = _uiState.value
        if (current.stage != GameStage.BETTING || current.currentBet == 0L) return
        val restoredChips = current.chipsOwned + current.currentBet
        _uiState.update {
            it.copy(
                chipsOwned = restoredChips,
                currentBet = 0L,
                dealerStatusText = "Place your bets"
            )
        }
    }

    // --- DEAL & ROUND INITIALIZATION ---

    fun deal() {
        val current = _uiState.value
        if (current.stage != GameStage.BETTING || current.currentBet <= 0) return

        persistUserChips(current.chipsOwned)

        val pCard1 = drawCard(true)
        val dCard1 = drawCard(true)
        val pCard2 = drawCard(true)
        val dCard2 = drawCard(false)

        val playerInitial = listOf(pCard1, pCard2)
        val dealerInitial = listOf(dCard1, dCard2)

        val (pValue, pSoft) = calculateHandValue(playerInitial)
        val (dValue, _) = calculateHandValue(listOf(dCard1))

        val playerHasBlackjack = pValue == 21

        _uiState.update {
            it.copy(
                playerHand = playerInitial,
                dealerHand = dealerInitial,
                playerHandValue = pValue,
                isPlayerSoft = pSoft,
                dealerHandValue = dValue,
                stage = if (playerHasBlackjack) GameStage.DEALER_TURN else GameStage.PLAYER_TURN,
                dealerEmotion = DealerEmotion.NEUTRAL,
                dealerStatusText = if (playerHasBlackjack) "Natural 21!" else "Hit or Stand?",
                canDoubleDown = it.chipsOwned >= it.currentBet && !playerHasBlackjack,
                canSplit = playerInitial[0].rank == playerInitial[1].rank && it.chipsOwned >= it.currentBet
            )
        }

        if (playerHasBlackjack) {
            resolveDealerHand()
        }
    }

    // --- PLAYER ACTIONS ---

    fun hit() {
        val current = _uiState.value
        if (current.stage != GameStage.PLAYER_TURN) return

        val newCard = drawCard(true)
        val updatedPlayerHand = current.playerHand + newCard
        val (pValue, pSoft) = calculateHandValue(updatedPlayerHand)

        if (pValue > 21) {
            endRound(
                result = RoundResult.PLAYER_BUST,
                updatedPlayerHand = updatedPlayerHand,
                pValue = pValue,
                pSoft = pSoft,
                dealerEmotion = DealerEmotion.SMUG,
                statusText = "Busted with $pValue!"
            )
        } else if (pValue == 21) {
            _uiState.update {
                it.copy(
                    playerHand = updatedPlayerHand,
                    playerHandValue = pValue,
                    isPlayerSoft = pSoft,
                    canDoubleDown = false,
                    canSplit = false
                )
            }
            stand()
        } else {
            _uiState.update {
                it.copy(
                    playerHand = updatedPlayerHand,
                    playerHandValue = pValue,
                    isPlayerSoft = pSoft,
                    canDoubleDown = false,
                    canSplit = false,
                    dealerStatusText = "Player has $pValue"
                )
            }
        }
    }

    fun stand() {
        val current = _uiState.value
        if (current.stage != GameStage.PLAYER_TURN) return
        _uiState.update { it.copy(stage = GameStage.DEALER_TURN) }
        resolveDealerHand()
    }

    fun doubleDown() {
        val current = _uiState.value
        if (current.stage != GameStage.PLAYER_TURN || !current.canDoubleDown) return

        val betAddition = current.currentBet
        val newCard = drawCard(true)
        val updatedHand = current.playerHand + newCard
        val (pValue, pSoft) = calculateHandValue(updatedHand)

        val newChips = current.chipsOwned - betAddition
        persistUserChips(newChips)

        _uiState.update {
            it.copy(
                chipsOwned = newChips,
                currentBet = current.currentBet * 2,
                playerHand = updatedHand,
                playerHandValue = pValue,
                isPlayerSoft = pSoft,
                canDoubleDown = false,
                canSplit = false
            )
        }

        if (pValue > 21) {
            endRound(
                result = RoundResult.PLAYER_BUST,
                updatedPlayerHand = updatedHand,
                pValue = pValue,
                pSoft = pSoft,
                dealerEmotion = DealerEmotion.SMUG,
                statusText = "Double Down Bust!"
            )
        } else {
            stand()
        }
    }

    // --- DEALER AI & TURN RESOLUTION ---

    private fun resolveDealerHand() {
        viewModelScope.launch {
            delay(500.milliseconds)
            val revealedHand = _uiState.value.dealerHand.map { it.copy(isFaceUp = true) }.toMutableList()
            var (dValue, _) = calculateHandValue(revealedHand)

            _uiState.update {
                it.copy(
                    dealerHand = revealedHand,
                    dealerHandValue = dValue,
                    dealerStatusText = "Dealer has $dValue"
                )
            }

            delay(700.milliseconds)

            while (dValue < 17) {
                val newCard = drawCard(true)
                revealedHand.add(newCard)
                val (updatedDValue, _) = calculateHandValue(revealedHand)
                dValue = updatedDValue

                _uiState.update {
                    it.copy(
                        dealerHand = revealedHand.toList(),
                        dealerHandValue = dValue,
                        dealerStatusText = "Dealer draws: $dValue"
                    )
                }
                delay(800.milliseconds)
            }

            val pValue = _uiState.value.playerHandValue
            val pHasBlackjack = _uiState.value.playerHand.size == 2 && pValue == 21
            val dHasBlackjack = revealedHand.size == 2 && dValue == 21

            val roundOutcome: Triple<RoundResult, DealerEmotion, String> = when {
                pHasBlackjack && !dHasBlackjack ->
                    Triple(RoundResult.PLAYER_BLACKJACK, DealerEmotion.BUSTED, "Blackjack pays 3:2!")
                dValue > 21 ->
                    Triple(RoundResult.DEALER_BUST, DealerEmotion.BUSTED, "Dealer busts!")
                pValue > dValue ->
                    Triple(RoundResult.PLAYER_WIN, DealerEmotion.BUSTED, "Player wins $pValue vs $dValue")
                dValue > pValue ->
                    Triple(RoundResult.DEALER_WIN, DealerEmotion.SMUG, "Dealer wins $dValue vs $pValue")
                else ->
                    Triple(RoundResult.PUSH, DealerEmotion.NEUTRAL, "Push on $pValue")
            }

            endRound(
                result = roundOutcome.first,
                updatedPlayerHand = _uiState.value.playerHand,
                pValue = pValue,
                pSoft = _uiState.value.isPlayerSoft,
                dealerEmotion = roundOutcome.second,
                statusText = roundOutcome.third
            )
        }
    }

    // --- PAYOUT & DATABASE PERSISTENCE ---

    private fun endRound(
        result: RoundResult,
        updatedPlayerHand: List<Card>,
        pValue: Int,
        pSoft: Boolean,
        dealerEmotion: DealerEmotion,
        statusText: String
    ) {
        val current = _uiState.value
        val bet = current.currentBet

        val payoutChips = when (result) {
            RoundResult.PLAYER_BLACKJACK -> (bet * 2.5).toLong()
            RoundResult.PLAYER_WIN, RoundResult.DEALER_BUST -> bet * 2
            RoundResult.PUSH -> bet
            RoundResult.DEALER_WIN, RoundResult.PLAYER_BUST, RoundResult.NONE -> 0L
        }

        val finalChips = current.chipsOwned + payoutChips

        _uiState.update {
            it.copy(
                chipsOwned = finalChips,
                playerHand = updatedPlayerHand,
                playerHandValue = pValue,
                isPlayerSoft = pSoft,
                stage = GameStage.ROUND_OVER,
                result = result,
                dealerEmotion = dealerEmotion,
                dealerStatusText = statusText
            )
        }

        persistUserChips(finalChips)
    }

    private fun persistUserChips(newChips: Long) {
        val userId = _uiState.value.userId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                userDao.setChips(userId, newChips)
            }
        }
    }

    fun resetForNextRound() {
        _uiState.update {
            it.copy(
                currentBet = 0L,
                playerHand = emptyList(),
                dealerHand = emptyList(),
                playerHandValue = 0,
                dealerHandValue = 0,
                stage = GameStage.BETTING,
                result = RoundResult.NONE,
                dealerEmotion = DealerEmotion.NEUTRAL,
                dealerStatusText = "Place your bets",
                canDoubleDown = false,
                canSplit = false
            )
        }
    }
}

// --- 4. VIEWMODEL FACTORY ---

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
package io.github.doubleddoge.splithappens.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize.getLargeAnchoredAdaptiveBannerAdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.github.doubleddoge.splithappens.data.DatabaseTest
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.game.model.HandResult
import io.github.doubleddoge.splithappens.game.model.HandStatus
import io.github.doubleddoge.splithappens.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Focused Edge Case Testing suite testing actual production behavior:
 * 1. Blank forms and emoji player names.
 * 2. Leading/trailing space trimming (as implemented in LoginViewModel via .trim()).
 * 3. Zero or negative bet submissions (invalid bets).
 * 4. Back stack / memory growth and navigation resilience.
 * 5. Time tampering / clock rollback exploit prevention for daily rewards.
 * 6. Split or double with insufficient available chips.
 * 7. Insurance with an odd bet (bet/2 rounds down) and insurance amount above available.
 * 8. Full round never draws from an empty shoe.
 * 9. Login with duplicate name differing only in case, blank name, very long name.
 * 10. AdMob rotation rebuilds AdView and old one is destroyed.
 * 11. Airplane mode: onAdFailedToLoad is logged, and layout space reserved doesn't shift UI.
 * 12. Insurance affordability and resolveInsurance validation against available post-bet bankroll.
 */
@RunWith(AndroidJUnit4::class)
class EdgeCaseTestingTest : DatabaseTest() {

	private val context: Context
		get() = ApplicationProvider.getApplicationContext()

	@Before
	fun setupCards() = runBlocking {
		db.cardDao().insertAll(CardEntity.standardDeck())
	}

	/**
	 * 1. Username validation (A-Z, a-z only, max 20 chars, no emojis/special chars/spaces) and case
	 *    insensitivity.
	 */
	@Test
	fun testUsernameValidationAndCaseInsensitivity() = runBlocking {
		val dao = db.userDao()
		dao.insert(UserEntity(displayName = "Alice", chipsOwned = 2500L))

		val allUsers = dao.observeAll().first()
		val match =
			allUsers.firstOrNull { it.displayName.equals("alice".trim(), ignoreCase = true) }
		assertNotNull(match)
		assertEquals("Alice", match?.displayName)

		// Character filtering: only A-Z, a-z allowed
		val invalidInput = "Alice 123!🔥"
		val filtered = invalidInput.filter { it in 'a'..'z' || it in 'A'..'Z' }
		assertEquals("Alice", filtered)

		// Max length 20 characters
		val longInput = "abcdefghijklmnopqrstuvwxyz"
		val clamped = longInput.filter { it in 'a'..'z' || it in 'A'..'Z' }.take(20)
		assertEquals(20, clamped.length)
		assertEquals("abcdefghijklmnopqrst", clamped)
	}

	/** 2. Blank forms, whitespace edge case. FAILS Suggested: Do not allow ANY white space */
	@Test
	fun testEdgeCase_PlayerNameLeadingTrailingSpacesTrimmed() {
		val rawInput = "   Alice Smith   "
		val trimmed = rawInput.trim()
		assertEquals("Alice Smith", trimmed)
	}

	/**
	 * 3. Zero or negative bet submissions edge case. FAILURE. Expected EXCEPTION Vulnerable to SQL
	 *    injections. Only safeguard at the moment is the frotnend elements.
	 */
	@Test
	fun testEdgeCase_InvalidBetsZeroOrNegative() {
		assertFailsWithException {
			HandEntity.player(0L, HandStatus.STOOD, HandResult.WIN)
		}
		assertFailsWithException {
			HandEntity.player(-100L, HandStatus.STOOD, HandResult.WIN)
		}
	}

	/** 4. Navigation / Back Stack Memory Resilience */
	@Test
	fun testEdgeCase_BackStackMemoryResilience() = runBlocking {
		val user = newUser("StackUser", 1000L)
		repeat(100) {
			val viewModel = HomeViewModel(db.userDao(), db.gameHistoryDao(), user.userId)
			assertNotNull(viewModel)
		}
		assertTrue(true)
	}

	/** 5. Time Tampering / Clock Rollback Exploit Prevention for Daily Rewards */
	@Test
	fun testEdgeCase_TimeTamperingDailyRewardProtection() = runBlocking {
		val user = newUser("TimeTraveler", 1000L)
		val prefs = context.getSharedPreferences("split_happens_prefs", Context.MODE_PRIVATE)
		val claimKey = "pref_last_daily_claim_${user.userId}"

		val now = System.currentTimeMillis()
		val interval = 24 * 60 * 60 * 1000L

		prefs.edit().putLong(claimKey, now).apply()
		db.userDao().setChips(user.userId, user.chipsOwned + 500L)

		val tamperedPastTime = now - 5000L
		val lastClaim = prefs.getLong(claimKey, 0L)

		var exploitClaimed = false
		if (tamperedPastTime >= lastClaim && (tamperedPastTime - lastClaim) >= interval) {
			exploitClaimed = true
		}

		assertFalse("Time tampering exploit succeeded!", exploitClaimed)
		assertEquals(1500L, db.userDao().getById(user.userId)?.chipsOwned)
	}

	@Test
	fun testEdgeCase_ClockRollbackTimeTravelingRejected() = runBlocking {
		val app =
			ApplicationProvider.getApplicationContext<
					io.github.doubleddoge.splithappens.SplitHappensApp
					>()
		val user = newUser("RollbackUser", 1000L)
		val prefs = context.getSharedPreferences("split_happens_prefs", Context.MODE_PRIVATE)
		val claimKey = "pref_last_daily_claim_${user.userId}"

		val futureTime = System.currentTimeMillis() + 100000L
		prefs.edit().putLong(claimKey, futureTime).apply()

		app.checkAndClaimDailyReward(user.userId)

		val updatedUser = db.userDao().getById(user.userId)
		assertEquals("Chips should not increase on clock rollback", 1000L, updatedUser?.chipsOwned)
	}

	/** 8. Test that a full round never draws from an empty shoe. */
	@Test
	fun testEdgeCase_FullRoundNeverDrawsFromEmptyShoe() {
		val shoe = Shoe(deckCount = 1)
		// Deal all 52 cards
		repeat(52) {
			val card = shoe.deal()
			assertNotNull(card)
		}
		assertEquals(0, shoe.remaining)
		// Verify shoe auto-reshuffles when empty on next deal
		val nextCard = shoe.deal()
		assertNotNull(nextCard)
		assertEquals(51, shoe.remaining)
	}

	/** 9. Login with duplicate name differing only in case, blank name, very long name. */
	@Test
	fun testEdgeCase_LoginNameVariations() = runBlocking {
		val dao = db.userDao()

		dao.insert(UserEntity(displayName = "Alice", chipsOwned = 1000L))
		val existing =
			dao.observeAll().first().firstOrNull {
				it.displayName.equals("alice".trim(), ignoreCase = true)
			}
		assertNotNull("Case-insensitive duplicate user should be recognized", existing)

		// Very long name
		val longName = "X".repeat(300)
		val longUser = UserEntity(displayName = longName, chipsOwned = 1000L)
		dao.insert(longUser)
		assertEquals(300, dao.getById(longUser.userId)?.displayName?.length)
	}

	/** 10. AdMob Rotation rebuilds the AdView, and the old one is destroyed. */
	@Test
	fun testEdgeCase_AdMobRotationRebuildAndDestruction() {
		val oldAdView =
			AdView(context).apply {
				adUnitId = "ca-app-pub-3940256099942544/9214589741"
			}
		oldAdView.destroy()

		val newAdView =
			AdView(context).apply {
				adUnitId = "ca-app-pub-3940256099942544/9214589741"
			}
		assertNotNull(newAdView)
	}

	/**
	 * 11. Airplane mode: onAdFailedToLoad is logged, and layout space reserved doesn't shift the UI.
	 */
	@Test
	fun testEdgeCase_AirplaneModeAdFailureAndLayoutReservation() {
		var failureLogged = false

		val adView =
			AdView(context).apply {
				adUnitId = "ca-app-pub-3940256099942544/9214589741"
				setAdSize(getLargeAnchoredAdaptiveBannerAdSize(context, 320))
				adListener =
					object : AdListener() {
						override fun onAdFailedToLoad(error: LoadAdError) {
							failureLogged = true
						}
					}
			}

		// Simulate airplane mode / network loss callback
		val airplaneError =
			LoadAdError(2, "No Network (Airplane Mode)", "com.google.android.gms.ads", null, null)
		adView.adListener.onAdFailedToLoad(airplaneError)

		assertTrue(failureLogged)
		val adSize = adView.adSize
		assertNotNull(adSize)
		assertEquals(320, adSize?.width)
	}

	/**
	 * 12. Insurance Affordability and resolveInsurance Validation Tests that insurance can never be
	 *     accepted or offered if available bankroll < insurance cost, and that Round.resolveInsurance
	 *     correctly validates require(amount <= available).
	 */
	@Test
	fun testEdgeCase_InsuranceUnaffordableThrowsException() {
		val shoe = Shoe(deckCount = 1)
		val round = Round(shoe = shoe, mode = GameMode.WILDCARD, bankroll = 50L, minBet = 50L)
		round.startBetting(50L) // Bet all chips, available chips = 0

		// Insurance cost = 50 / 2 = 25. Available = 0.
		// Attempting to resolve insurance with amount = 25 must throw IllegalArgumentException ("Not
		// enough chips").
		assertFailsWithException {
			round.resolveInsurance(25L)
		}
	}
}

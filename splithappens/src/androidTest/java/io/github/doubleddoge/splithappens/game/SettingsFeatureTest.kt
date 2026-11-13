package io.github.doubleddoge.splithappens.game

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.doubleddoge.splithappens.data.DatabaseTest
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.game.model.GameMode
import io.github.doubleddoge.splithappens.game.model.GameStage
import io.github.doubleddoge.splithappens.viewmodel.SettingViewModel
import io.github.doubleddoge.splithappens.viewmodel.uistate.GameUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFeatureTest : DatabaseTest() {

	@Before
	fun setupCards() = runBlocking {
		db.cardDao().insertAll(CardEntity.standardDeck())
	}

	@Test
	fun testSettingsViewModel_TogglesAndPreferences() {
		val app = ApplicationProvider.getApplicationContext<android.app.Application>()
		val settingViewModel = SettingViewModel(app)

		// Initial states
		val initialUiState = settingViewModel.uiState.value

		// 1. Dark Theme toggle
		settingViewModel.toggleDarkTheme(!initialUiState.darkTheme)
		assertEquals(!initialUiState.darkTheme, settingViewModel.uiState.value.darkTheme)

		// 2. Show Card Total toggle
		settingViewModel.toggleShowCardTotal(!initialUiState.showCardTotal)
		assertEquals(!initialUiState.showCardTotal, settingViewModel.uiState.value.showCardTotal)

		// 3. Dealer Rule Banner toggle
		settingViewModel.toggleDealerRuleBanner(!initialUiState.dealerRuleBanner)
		assertEquals(
			!initialUiState.dealerRuleBanner,
			settingViewModel.uiState.value.dealerRuleBanner
		)

		// 4. Sound Effects toggle
		settingViewModel.toggleSoundEffects(!initialUiState.soundEffects)
		assertEquals(!initialUiState.soundEffects, settingViewModel.uiState.value.soundEffects)

		// 5. Haptic Vibration toggle
		settingViewModel.toggleHapticVibration(!initialUiState.hapticVibration)
		assertEquals(
			!initialUiState.hapticVibration,
			settingViewModel.uiState.value.hapticVibration
		)

		// 6. Fast Deal toggle
		settingViewModel.toggleFastDeal(!initialUiState.fastDeal)
		assertEquals(!initialUiState.fastDeal, settingViewModel.uiState.value.fastDeal)

		// 7. Auto-Stand on 21 toggle
		settingViewModel.toggleAutoStandOn21(!initialUiState.autoStandOn21)
		assertEquals(!initialUiState.autoStandOn21, settingViewModel.uiState.value.autoStandOn21)
	}

	@Test
	fun testAutoStandOn21SettingBehavior() = runBlocking {
		val shoe = Shoe(deckCount = 1)
		val roundTrue =
			Round(shoe = shoe, mode = GameMode.TRADITIONAL, bankroll = 1000L, autoStandOn21 = true)
		assertNotNull(roundTrue)

		val roundFalse =
			Round(shoe = shoe, mode = GameMode.TRADITIONAL, bankroll = 1000L, autoStandOn21 = false)
		assertNotNull(roundFalse)
	}

	@Test
	fun testGameInProgress_states() {
		assertFalse(
			GameUiState(stage = GameStage.BETTING)
				.isGameInProgress
		)
		assertTrue(
			GameUiState(stage = GameStage.PLAYER_TURN)
				.isGameInProgress
		)
		assertFalse(
			GameUiState(stage = GameStage.DEALER_TURN)
				.isGameInProgress
		)
		assertFalse(
			GameUiState(stage = GameStage.ROUND_OVER)
				.isGameInProgress
		)
	}
}

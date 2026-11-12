package io.github.doubleddoge.splithappens

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import io.github.doubleddoge.splithappens.data.AppDatabase
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import io.github.doubleddoge.splithappens.data.getDatabase
import io.github.doubleddoge.splithappens.data.pref.PrefFiles
import io.github.doubleddoge.splithappens.data.pref.PrefKeys
import io.github.doubleddoge.splithappens.util.DailyReward
import io.github.doubleddoge.splithappens.util.PlayerName.LEGACY_DEFAULT_USER_ID
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "SplitHappensApp"

class SplitHappensApp : Application() {
	val database: AppDatabase by lazy { getDatabase(this) }
	private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	lateinit var seedJob: Job
		private set

	override fun onCreate() {
		super.onCreate()
		seedJob = appScope.launch {
			runCatching { seedDefaults() }.onFailure { Log.e(TAG, "Seeding defaults failed", it) }
			runCatching { checkAndClaimDailyReward() }
				.onFailure { Log.e(TAG, "Daily reward check failed", it) }
		}
	}

	private suspend fun seedDefaults() {
		database.cardDao().insertAll(CardEntity.standardDeck()) // idempotent (ignores existing rows)
		migrateLegacyDefaultPlayer()
	}

	private suspend fun migrateLegacyDefaultPlayer() {
		val userDao = database.userDao()
		val legacyPlayer = userDao.getById(LEGACY_DEFAULT_USER_ID) ?: return

		val newUserId = UUID.randomUUID().toString()
		val migratedUser = UserEntity(
			userId = newUserId,
			displayName = legacyPlayer.displayName,
			chipsOwned = legacyPlayer.chipsOwned,
			createdAt = legacyPlayer.createdAt,
		)

		userDao.insert(migratedUser)
		database.sessionDao().reassignUserId(LEGACY_DEFAULT_USER_ID, newUserId)
		userDao.reassignProfilePictureUserId(LEGACY_DEFAULT_USER_ID, newUserId)
		userDao.delete(legacyPlayer)

		val authPrefs = getSharedPreferences(PrefFiles.AUTH, MODE_PRIVATE)
		val appPrefs = getSharedPreferences(PrefFiles.APP, MODE_PRIVATE)

		val lastClaim = appPrefs.getLong(PrefKeys.lastDailyClaim(LEGACY_DEFAULT_USER_ID), 0L)
		if (lastClaim > 0L) {
			appPrefs.edit {
				putLong(PrefKeys.lastDailyClaim(newUserId), lastClaim)
				remove(PrefKeys.lastDailyClaim(LEGACY_DEFAULT_USER_ID))
			}
		}

		val lastUsed = authPrefs.getLong(PrefKeys.lastUsed(LEGACY_DEFAULT_USER_ID), 0L)
		if (lastUsed > 0L) {
			authPrefs.edit {
				putLong(PrefKeys.lastUsed(newUserId), lastUsed)
				remove(PrefKeys.lastUsed(LEGACY_DEFAULT_USER_ID))
			}
		}

		if (authPrefs.getString(PrefKeys.SAVED_USER_ID, null) == LEGACY_DEFAULT_USER_ID) {
			authPrefs.edit {
				putString(PrefKeys.SAVED_USER_ID, newUserId)
				putString(PrefKeys.SAVED_USERNAME, migratedUser.displayName)
			}
		}
		if (appPrefs.getString(PrefKeys.ACTIVE_USER_ID, null) == LEGACY_DEFAULT_USER_ID) {
			appPrefs.edit { putString(PrefKeys.ACTIVE_USER_ID, newUserId) }
		}
	}

	suspend fun checkAndClaimDailyReward(targetUserId: String? = null) {
		val userDao = database.userDao()
		val prefs = getSharedPreferences(PrefFiles.APP, MODE_PRIVATE)
		val authPrefs = getSharedPreferences(PrefFiles.AUTH, MODE_PRIVATE)

		val activeUserId =
			targetUserId?.takeIf { it.isNotBlank() }
				?: authPrefs.getString(PrefKeys.SAVED_USER_ID, null)
				?: prefs.getString(PrefKeys.ACTIVE_USER_ID, null)

		val user =
			if (!activeUserId.isNullOrBlank()) {
				userDao.getById(activeUserId)
			} else {
				userDao.observeAll().first().firstOrNull()
			} ?: return

		val claimKey = PrefKeys.lastDailyClaim(user.userId)
		val now = System.currentTimeMillis()

		if (DailyReward.isDue(prefs.getLong(claimKey, 0L), now)) {
			prefs.edit { putLong(claimKey, now) }
			addDailyReward(user.userId)
		}
	}

	suspend fun addDailyReward(userId: String, amount: Long = DailyReward.AMOUNT) {
		val userDao = database.userDao()
		val user = userDao.getById(userId) ?: return
		userDao.setChips(user.userId, user.chipsOwned + amount)
	}
}
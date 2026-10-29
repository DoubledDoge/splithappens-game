package io.github.doubleddoge.splithappens

import android.app.Application
import androidx.core.content.edit
import io.github.doubleddoge.splithappens.data.SplitHappensDatabase
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplitHappensApp : Application() {
  val database: SplitHappensDatabase by lazy { buildSplitHappensDatabase(this) }
  private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  lateinit var seedJob: Job
    private set

  override fun onCreate() {
    super.onCreate()
    seedJob = appScope.launch {
      runCatching { seedDefaults() }.onFailure { it.printStackTrace() }
      runCatching { checkAndClaimDailyReward() }.onFailure { it.printStackTrace() }
    }
  }

  private suspend fun seedDefaults() {
    database.cardDao().insertAll(CardEntity.standardDeck()) // idempotent (IGNORE)

    val userDao = database.userDao()
    if (userDao.observeAll().first().isEmpty()) {
      userDao.insert(UserEntity(displayName = "Player 1", chipsOwned = 2500))
    }
  }

  suspend fun checkAndClaimDailyReward(targetUserId: String? = null) {
    val userDao = database.userDao()
    val prefs = getSharedPreferences("split_happens_prefs", MODE_PRIVATE)
    val authPrefs = getSharedPreferences("split_happens_auth", MODE_PRIVATE)

    val activeUserId =
        targetUserId?.takeIf { it.isNotBlank() }
            ?: authPrefs.getString("saved_user_id", null)
            ?: prefs.getString("active_user_id", null)

    val user =
        if (!activeUserId.isNullOrBlank()) {
          userDao.getById(activeUserId)
        } else {
          userDao.observeAll().first().firstOrNull()
        } ?: return

    val lastClaim = prefs.getLong("pref_last_daily_claim_${user.userId}", 0L)
    val now = System.currentTimeMillis()
    val interval = 24 * 60 * 60 * 1000L // 24 hours
    if (lastClaim == 0L || (now - lastClaim) >= interval) {
      prefs.edit { putLong("pref_last_daily_claim_${user.userId}", now) }
      userDao.setChips(user.userId, user.chipsOwned + 500L)
    }
  }
}

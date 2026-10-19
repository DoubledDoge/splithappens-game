package io.github.doubleddoge.splithappens

import android.app.Application
import io.github.doubleddoge.splithappens.data.SplitHappensDatabase
import io.github.doubleddoge.splithappens.data.buildSplitHappensDatabase
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class SplitHappensApp : Application() {
    val database: SplitHappensDatabase by lazy { buildSplitHappensDatabase(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var seedJob: Job
        private set

    override fun onCreate() {
        super.onCreate()
      seedJob = appScope.launch {
    runCatching { seedDefaults() }.onFailure { it.printStackTrace() }
}
    }

    private suspend fun seedDefaults() {
        database.cardDao().insertAll(CardEntity.standardDeck()) // idempotent (IGNORE)

        val userDao = database.userDao()
        if (userDao.observeAll().first().isEmpty()) {
            userDao.insert(UserEntity(displayName = "Player 1", chipsOwned = 2500))
        }
    }
}

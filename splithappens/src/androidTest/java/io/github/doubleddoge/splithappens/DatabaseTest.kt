package io.github.doubleddoge.splithappens

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import io.github.doubleddoge.splithappens.data.SplitHappensDatabase
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before

// Fresh in-memory database per test
abstract class DatabaseTest {
    protected lateinit var db: SplitHappensDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db =
            Room.inMemoryDatabaseBuilder<SplitHappensDatabase>(context)
                .setDriver(AndroidSQLiteDriver())
                .build()
    }

    @After
    fun closeDb() = db.close()

    protected suspend fun newUser(name: String = "Player", chips: Long = 1000): UserEntity =
        UserEntity(displayName = name, chipsOwned = chips).also { db.userDao().insert(it) }

    protected fun assertFailsWithException(block: suspend () -> Unit) {
        val failed = runCatching { kotlinx.coroutines.runBlocking { block() } }.isFailure
        assertTrue("Expected an exception", failed)
    }
}
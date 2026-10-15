package io.github.doubleddoge.splithappens.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import io.github.doubleddoge.splithappens.data.dao.CardDao
import io.github.doubleddoge.splithappens.data.dao.GameHistoryDao
import io.github.doubleddoge.splithappens.data.dao.RoundDao
import io.github.doubleddoge.splithappens.data.dao.SessionDao
import io.github.doubleddoge.splithappens.data.dao.UserDao
import io.github.doubleddoge.splithappens.data.entity.CardEntity
import io.github.doubleddoge.splithappens.data.entity.GameEntity
import io.github.doubleddoge.splithappens.data.entity.HandCardEntity
import io.github.doubleddoge.splithappens.data.entity.HandEntity
import io.github.doubleddoge.splithappens.data.entity.ProfilePictureEntity
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import io.github.doubleddoge.splithappens.data.view.GameHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
	entities = [
		UserEntity::class,
		ProfilePictureEntity::class,
		SessionEntity::class,
		GameEntity::class,
		HandEntity::class,
		HandCardEntity::class,
		CardEntity::class
	],
	views = [GameHistory::class],
	version = 1
)
abstract class SplitHappensDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao
	abstract fun sessionDao(): SessionDao
	abstract fun cardDao(): CardDao
	abstract fun roundDao(): RoundDao
	abstract fun gameHistoryDao(): GameHistoryDao
}

@Volatile
private var INSTANCE: SplitHappensDatabase? = null

fun buildSplitHappensDatabase(context: Context): SplitHappensDatabase {
	return INSTANCE ?: synchronized(SplitHappensDatabase::class.java) {
		INSTANCE ?: run {
			val db = Room.databaseBuilder<SplitHappensDatabase>(
				context.applicationContext,
				"splithappens.db"
			)
				.setDriver(AndroidSQLiteDriver())
				.build()

			// Initialize default user asynchronously without blocking callback signatures
			CoroutineScope(Dispatchers.IO).launch {
				try {
					val userDao = db.userDao()
					if (userDao.getById("default_player") == null) {
						userDao.insert(
							UserEntity(
								userId = "default_player",
								displayName = "Player 1",
								chipsOwned = 2500L
							)
						)
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}

			db.also { INSTANCE = it }
		}
	}
}
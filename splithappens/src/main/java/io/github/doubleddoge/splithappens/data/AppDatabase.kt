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

private const val DATABASE_NAME = "splithappens.db"

@Database(
	entities =
		[
			UserEntity::class,
			ProfilePictureEntity::class,
			SessionEntity::class,
			GameEntity::class,
			HandEntity::class,
			HandCardEntity::class,
			CardEntity::class,
		],
	views = [GameHistory::class],
	version = 1,
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao
	abstract fun sessionDao(): SessionDao
	abstract fun cardDao(): CardDao
	abstract fun roundDao(): RoundDao
	abstract fun gameHistoryDao(): GameHistoryDao
}

@Volatile
private var instance: AppDatabase? = null

fun getDatabase(context: Context): AppDatabase =
	instance ?: synchronized(AppDatabase::class.java) {
		instance ?: Room.databaseBuilder<AppDatabase>(context.applicationContext, DATABASE_NAME)
			.setDriver(AndroidSQLiteDriver())
			.build()
			.also { instance = it }
	}
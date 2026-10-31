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
abstract class Database : RoomDatabase() {
  abstract fun userDao(): UserDao

  abstract fun sessionDao(): SessionDao

  abstract fun cardDao(): CardDao

  abstract fun roundDao(): RoundDao

  abstract fun gameHistoryDao(): GameHistoryDao
}

@Volatile private var INSTANCE: io.github.doubleddoge.splithappens.data.Database? = null

fun buildDatabase(context: Context): io.github.doubleddoge.splithappens.data.Database {
  return INSTANCE
      ?: synchronized(Database::class.java) {
        INSTANCE
            ?: run {
              val db =
                  Room.databaseBuilder<io.github.doubleddoge.splithappens.data.Database>(
                          context.applicationContext,
                          "splithappens.db",
                      )
                      .setDriver(AndroidSQLiteDriver())
                      .build()

              // Build the database instance
              db.also { INSTANCE = it }
            }
      }
}

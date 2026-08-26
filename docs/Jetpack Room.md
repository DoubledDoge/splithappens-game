# Jetpack Room

The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.

Newest Version: 2.8.4

## The 3 Key components of Room

- The [database class](https://developer.android.com/reference/kotlin/androidx/room3/Database) that holds the database and serves as the main access point for the underlying connection to our app's persisted data.

- The [data entities](https://developer.android.com/training/data-storage/room/defining-data) that represent tables.

- The [data access objects (DAOs)](https://developer.android.com/training/data-storage/room/accessing-data) that provide functions that our app can use to perform CRUD.

Supposing a hypothetical of our program having a database with 3 tables – records, session, and history.

Following best practices and the logical separation between the entities (tables), the system structure would be:

1. Database object (annotated with @database) – abstract class

2. DAOs – an Interface with methods - @insert, @query, @update, @delete

3. Entities – class annotated with @entity, representing the table & its schema.

If we want to use queries that would join tables, we can have another DAO.

## Suggested Schema

Table for tracking records

Table for tracking all the Sessions

Table for all-time statistics


Records and All Time Statistics would mainly feed and derive from the Session table.

### Session schema:

Sessions contain:

- SessionID (pk)

- Stakes

- Status

Games:

- GameID (pk)

- SessionID (fk)

- GameNum

- DealerTotal

Hands
- HandID (pk)

- GameID (fk)

- ParentHandID (nullable, fk to Hands)

- Owner

- BetAmount

- HandStatus – like standing, busted, split

- Result – win, loss, push (per hand for splitting)

Cards

- CardID (pk)

- HandID (fk)

- Suit

- Rank

- IsHoleCard

- DealtOrder

## Build.gradle.kts

```kts
val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")

ksp("androidx.room:room-compiler:$room_version")
```

Make sure that this KSP gradle plugin is in build.gradle too. (shouldn’t be necessary)

```kts
plugins {
    id("com.google.devtools.ksp") version "<version>"
}
```

Sample implementations:

Data entity Session. Each instance of Session represents a row in a Session table in the app’s database.

```kt
@Entity(tableName = "Sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val SessionId: Long = 0,
    val stakes: Int,
    val status: String
)
```

Data access object SessionDao. Provides the functions that the rest of the app uses to interact with data in the Session table.

```kt
@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(Session: SessionEntity): Long

    @Query("SELECT * FROM Sessions WHERE SessionId = :id")
    suspend fun getSessionById(id: Long): SessionEntity?
}
```

Room Database

```kt
@Database(entities = [SessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun SessionDao(): SessionDao
}
```

Create an instance of the database

```kt
val db =
    Room.databaseBuilder<AppDatabase>(applicationContext, "database-name")
        .setDriver(AndroidSQLiteDriver())
        .build()
```

You can then use the abstract functions from the AppDatabase to get an instance of the DAO. In turn, you can use the functions from the DAO instance to interact with the database:

```kt
val SessionDao = db.SessionDao()
val users: List<Session> = SessionDao.getSessionById()
```

## Type Conversion

Will need TypeConverters, since room only supports basic primitive types. Converter functions help to map things like enum to raw types (strings or ints)

Firstly define the enums 

```kt
enum class HandStatus {
    ACTIVE,
    STOOD,
    BUSTED,
    BLACKJACK,
    SURRENDERED,
    SPLIT
}

enum class SessionStatus {
    ONGOING,
    COMPLETED,
}

enum class HandResult {
    IN_PROGRESS,
    WIN,
    LOSS,
    PUSH
}
```
TypeConverters Class

```kt
class Converters {

    @TypeConverter
    fun fromSessionStatus(status: SessionStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus? {
        return value?.let { enumValueOf<SessionStatus>(it) }
    }


    @TypeConverter
    fun fromHandStatus(status: HandStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toHandStatus(value: String?): HandStatus? {
        return value?.let { enumValueOf<HandStatus>(it) }
    }

    @TypeConverter
    fun fromHandResult(result: HandResult?): String? {
        return result?.name
    }

    @TypeConverter
    fun toHandResult(value: String?): HandResult? {
        return value?.let { enumValueOf<HandResult>(it) }
    }




    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

Sources

https://developer.android.com/training/data-storage/room 

https://developer.android.com/jetpack/androidx/releases/room 

https://developer.android.com/training/data-storage/room/referencing-data 

(unused source, but recommended)

https://medium.com/@manishkumar_75473/jetpack-room-in-android-a-step-by-step-guide-with-note-taking-app-example-a80a8791ee06 

# Jetpack Room

The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.

Version in use: **3.0.3** (`androidx.room3`, released 2026-09-09).

## Room 3 vs. the 2.x tutorials

Most tutorials online (including the Medium article this doc used to link) describe Room 2.x. The differences that matter for us:

- New package and artifacts: `androidx.room3.*`, `androidx.room3:room3-runtime`, `androidx.room3:room3-compiler`.
- KSP is required. Room 3 only generates Kotlin; KAPT and Java annotation processing are gone.
- A `SQLiteDriver` is required to build a database (`setDriver(...)`).
- DAO functions must be `suspend`, or return a reactive type such as `Flow`.
- `@TypeConverter` is renamed `@ColumnTypeConverter`.
- `SupportSQLite` APIs are gone; for example `runInTransaction { }` becomes `withWriteTransaction { }`.
- No `Executor` configuration; a `CoroutineContext` is set on the builder instead.

## The 3 Key components of Room

- The [database class](https://developer.android.com/reference/kotlin/androidx/room3/Database) that holds the database and serves as the main access point for the underlying connection to our app's persisted data.

- The [data entities](https://developer.android.com/training/data-storage/room/defining-data) that represent tables.

- The [data access objects (DAOs)](https://developer.android.com/training/data-storage/room/accessing-data) that provide functions that our app can use to perform CRUD.

Following best practices and the logical separation between the entities (tables), the system structure is:

1. Database object (annotated with `@Database`) – abstract class extending `RoomDatabase`
2. DAOs – interfaces with `suspend` / `Flow` functions annotated `@Insert`, `@Query`, `@Update`, `@Delete`
3. Entities – classes annotated with `@Entity`, representing a table and its schema

If we want queries that join tables, we can have another DAO.

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

Hands:

- HandID (pk)
- GameID (fk)
- ParentHandID (nullable, fk to Hands)
- Owner
- BetAmount
- HandStatus – like standing, busted, split
- Result – win, loss, push (per hand for splitting)

Cards:

- CardID (pk)
- HandID (fk)
- Suit
- Rank
- IsHoleCard
- DealtOrder

## Gradle setup

Version catalog (`gradle/libs.versions.toml`):

```toml
[versions]
ksp = "2.3.11"
room3 = "3.0.3"

[libraries]
androidx-room3-runtime = { group = "androidx.room3", name = "room3-runtime", version.ref = "room3" }
androidx-room3-compiler = { group = "androidx.room3", name = "room3-compiler", version.ref = "room3" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
androidx-room3 = { id = "androidx.room3", version.ref = "room3" }
```

Root `build.gradle.kts`:

```kts
plugins {
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.androidx.room3) apply false
}
```

Module `splithappens/build.gradle.kts`:

```kts
plugins {
	alias(libs.plugins.ksp)
	alias(libs.plugins.androidx.room3)
}

room3 {
	schemaDirectory("$projectDir/schemas")
}

dependencies {
	implementation(libs.androidx.room3.runtime)
	ksp(libs.androidx.room3.compiler)
}
```

Notes:

- KSP is not optional: Room 3 will not build without it. AGP 9 requires KSP 2.3.6 or higher.
- AGP 9 has built-in Kotlin, so there is no `kotlin-android` plugin and no `android.builtInKotlin` flag.
- `schemaDirectory` is required by the Room Gradle plugin. The generated schema JSON files (`schemas/<package>.<DatabaseClass>/<version>.json`) are **checked into git**; they are used for validation and auto-migrations.
- Because the schema directory is keyed by the database class's fully qualified name, the package must be final before the first build.

## Sample implementations

Data entity. Each instance of `SessionEntity` represents a row in the `Sessions` table.

```kt
@Entity(tableName = "Sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val stakes: Int,
    val status: SessionStatus
)
```

Data access object. Provides the functions the rest of the app uses to interact with the `Sessions` table.

```kt
@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM Sessions WHERE sessionId = :id")
    suspend fun getSessionById(id: Long): SessionEntity?
}
```

Room database:

```kt
@Database(entities = [SessionEntity::class], version = 1)
abstract class SplitHappensDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
```

Create an instance of the database. A `SQLiteDriver` is required; we use the platform's SQLite via `AndroidSQLiteDriver`. (The alternative is `BundledSQLiteDriver` from `androidx.sqlite:sqlite-bundled`, which ships its own SQLite build at the cost of APK size.) The app runs in a single process, so keep one instance.

```kt
val db =
    Room.databaseBuilder<SplitHappensDatabase>(applicationContext, "splithappens.db")
        .setDriver(AndroidSQLiteDriver())
        .build()
```

DAO functions are `suspend`, so call them from a coroutine:

```kt
val sessionDao = db.sessionDao()
val session: SessionEntity? = sessionDao.getSessionById(id)
```

## Type Conversion

Room only supports basic column types out of the box. Enums are stored by name as text without a converter, so the enums below need no converter of their own:

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

Anything else needs a converter. In Room 3 the annotation is `@ColumnTypeConverter`, and the class is registered on the database with `@ColumnTypeConverters`:

```kt
class Converters {

    @ColumnTypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @ColumnTypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

```kt
@Database(entities = [SessionEntity::class], version = 1)
@ColumnTypeConverters(Converters::class)
abstract class SplitHappensDatabase : RoomDatabase() { /* ... */ }
```

## Sources

https://developer.android.com/training/data-storage/room

https://developer.android.com/jetpack/androidx/releases/room3

https://developer.android.com/training/data-storage/room/referencing-data
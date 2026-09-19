package io.github.doubleddoge.splithappens.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import io.github.doubleddoge.splithappens.data.entity.ProfilePictureEntity
import io.github.doubleddoge.splithappens.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

	@Insert
	suspend fun insert(user: UserEntity)

	@Update
	suspend fun update(user: UserEntity)

	@Delete
	suspend fun delete(user: UserEntity)

	@Query("SELECT * FROM Users ORDER BY displayName")
	fun observeAll(): Flow<List<UserEntity>>

	@Query("SELECT * FROM Users WHERE userId = :userId")
	fun observeById(userId: String): Flow<UserEntity?>

	@Query("SELECT * FROM Users WHERE userId = :userId")
	suspend fun getById(userId: String): UserEntity?

	@Query("UPDATE Users SET chipsOwned = :chips WHERE userId = :userId")
	suspend fun setChips(userId: String, chips: Long)

	// ProfilePictures is a leaf table, nothing cascades from it.
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun savePicture(picture: ProfilePictureEntity)

	@Query("SELECT image FROM ProfilePictures WHERE userId = :userId")
	suspend fun getPicture(userId: String): ByteArray?
}

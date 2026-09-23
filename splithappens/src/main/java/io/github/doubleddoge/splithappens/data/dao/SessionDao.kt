package io.github.doubleddoge.splithappens.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import io.github.doubleddoge.splithappens.data.entity.SessionEntity
import io.github.doubleddoge.splithappens.data.enum.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM Sessions WHERE sessionId = :sessionId")
    suspend fun getById(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM Sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun observeForUser(userId: String): Flow<List<SessionEntity>>

    @Query("UPDATE Sessions SET status = :status, endedAt = :endedAt WHERE sessionId = :sessionId")
    suspend fun setStatus(
        sessionId: Long,
        status: SessionStatus,
        endedAt: Long?,
    )
}

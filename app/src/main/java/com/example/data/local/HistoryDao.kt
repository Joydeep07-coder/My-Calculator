package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query(
        "SELECT * FROM calculation_history " +
            "WHERE expression = :expression AND result = :result AND toolName = :toolName " +
            "LIMIT 1"
    )
    suspend fun findDuplicate(expression: String, result: String, toolName: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val expression: String,
    val result: String,
    val toolName: String,
    val reusableRoute: String,
    val timestampMillis: Long,
)

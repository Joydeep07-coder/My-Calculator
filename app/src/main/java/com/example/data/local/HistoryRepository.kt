package com.example.data.local

import com.example.model.HistoryEntry
import com.example.model.HistoryGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

interface HistoryRepository {
    val historyEntries: Flow<List<HistoryEntry>>

    suspend fun addEntry(
        expression: String,
        result: String,
        toolName: String,
        reusableRoute: String,
        timestampMillis: Long = System.currentTimeMillis(),
    ): Boolean

    suspend fun deleteById(id: String)

    suspend fun clearAll()
}

object HistoryValidation {
    private val invalidResults = setOf(
        "Error",
        "NaN",
        "Infinity",
        "-Infinity",
        "Undefined",
        "—",
    )

    fun isValid(expression: String, result: String, toolName: String): Boolean {
        val expr = expression.trim()
        val res = result.trim()
        if (expr.isEmpty() || res.isEmpty() || toolName.isBlank()) return false
        if (res in invalidResults) return false
        return true
    }

    fun groupForTimestamp(
        timestampMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
    ): HistoryGroup {
        val targetCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val todayCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return when {
            isSameDay(targetCal, todayCal) -> HistoryGroup.TODAY
            isSameDay(targetCal, yesterdayCal) -> HistoryGroup.YESTERDAY
            else -> HistoryGroup.EARLIER
        }
    }

    fun formatTimestamp(
        timestampMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
    ): String {
        val diff = nowMillis - timestampMillis
        if (diff in 0L..59_999L) return "Just now"
        val group = groupForTimestamp(timestampMillis, nowMillis)
        val date = Date(timestampMillis)
        return when (group) {
            HistoryGroup.TODAY -> SimpleDateFormat("h:mm a", Locale.US).format(date)
            HistoryGroup.YESTERDAY -> "Yesterday, " + SimpleDateFormat("h:mm a", Locale.US).format(date)
            HistoryGroup.EARLIER -> SimpleDateFormat("MMM d, h:mm a", Locale.US).format(date)
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

fun HistoryEntity.toDomainModel(nowMillis: Long = System.currentTimeMillis()): HistoryEntry =
    HistoryEntry(
        id = id,
        expression = expression,
        result = result,
        toolName = toolName,
        timeLabel = HistoryValidation.formatTimestamp(timestampMillis, nowMillis),
        group = HistoryValidation.groupForTimestamp(timestampMillis, nowMillis),
        reusableRoute = reusableRoute,
        timestampMillis = timestampMillis,
    )

class RoomHistoryRepository(
    private val historyDao: HistoryDao,
) : HistoryRepository {

    override val historyEntries: Flow<List<HistoryEntry>> = historyDao.observeAll().map { entities ->
        val now = System.currentTimeMillis()
        entities.map { it.toDomainModel(now) }
    }

    override suspend fun addEntry(
        expression: String,
        result: String,
        toolName: String,
        reusableRoute: String,
        timestampMillis: Long,
    ): Boolean {
        val cleanExpr = expression.trim()
        val cleanResult = result.trim()
        val cleanTool = toolName.trim()
        if (!HistoryValidation.isValid(cleanExpr, cleanResult, cleanTool)) {
            return false
        }

        val existing = historyDao.findDuplicate(cleanExpr, cleanResult, cleanTool)
        val entity = if (existing != null) {
            // Avoid duplicate rows; refresh timestamp so re-evaluated entries move to top.
            existing.copy(
                reusableRoute = reusableRoute,
                timestampMillis = timestampMillis,
            )
        } else {
            HistoryEntity(
                id = UUID.randomUUID().toString(),
                expression = cleanExpr,
                result = cleanResult,
                toolName = cleanTool,
                reusableRoute = reusableRoute,
                timestampMillis = timestampMillis,
            )
        }
        historyDao.upsert(entity)
        return true
    }

    override suspend fun deleteById(id: String) {
        historyDao.deleteById(id)
    }

    override suspend fun clearAll() {
        historyDao.clearAll()
    }
}

class InMemoryHistoryRepository : HistoryRepository {
    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    override val historyEntries: Flow<List<HistoryEntry>> = _entries.asStateFlow()

    override suspend fun addEntry(
        expression: String,
        result: String,
        toolName: String,
        reusableRoute: String,
        timestampMillis: Long,
    ): Boolean {
        val cleanExpr = expression.trim()
        val cleanResult = result.trim()
        val cleanTool = toolName.trim()
        if (!HistoryValidation.isValid(cleanExpr, cleanResult, cleanTool)) {
            return false
        }
        _entries.update { current ->
            val existing = current.firstOrNull {
                it.expression == cleanExpr && it.result == cleanResult && it.toolName == cleanTool
            }
            val updated = HistoryEntry(
                id = existing?.id ?: UUID.randomUUID().toString(),
                expression = cleanExpr,
                result = cleanResult,
                toolName = cleanTool,
                timeLabel = HistoryValidation.formatTimestamp(timestampMillis),
                group = HistoryValidation.groupForTimestamp(timestampMillis),
                reusableRoute = reusableRoute,
                timestampMillis = timestampMillis,
            )
            listOf(updated) + current.filterNot { it.id == updated.id }
        }
        return true
    }

    override suspend fun deleteById(id: String) {
        _entries.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun clearAll() {
        _entries.value = emptyList()
    }
}

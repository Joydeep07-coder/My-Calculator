package com.example.state

import androidx.lifecycle.ViewModel
import com.example.data.SampleCatalogs
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.HistoryEntry
import com.example.model.HistoryGroup
import com.example.model.ThemeMode
import com.example.model.ToolItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class OmniCalcViewModel : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _recentToolIds = MutableStateFlow<List<String>>(emptyList())
    val recentToolIds: StateFlow<List<String>> = _recentToolIds.asStateFlow()

    private val _reusedExpression = MutableStateFlow<String?>(null)
    val reusedExpression: StateFlow<String?> = _reusedExpression.asStateFlow()

    private val _lastPreviewExpression = MutableStateFlow("")
    val lastPreviewExpression: StateFlow<String> = _lastPreviewExpression.asStateFlow()

    private val _lastPreviewResult = MutableStateFlow("0")
    val lastPreviewResult: StateFlow<String> = _lastPreviewResult.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun markOnboardingCompleted() {
        _onboardingCompleted.value = true
    }

    fun setThemeMode(mode: ThemeMode) {
        _settings.update { it.copy(themeMode = mode) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        _settings.update { it.copy(hapticFeedback = enabled) }
    }

    fun setButtonSound(enabled: Boolean) {
        _settings.update { it.copy(buttonSound = enabled) }
    }

    fun setDecimalPrecision(precision: Int) {
        _settings.update { it.copy(decimalPrecision = precision.coerceIn(2, 10)) }
    }

    fun setThousandsSeparator(enabled: Boolean) {
        _settings.update { it.copy(thousandsSeparator = enabled) }
    }

    fun setAngleMode(mode: AngleMode) {
        _settings.update { it.copy(angleMode = mode) }
    }

    fun setLanguage(language: String) {
        _settings.update { it.copy(language = language) }
    }

    fun markToolUsed(toolId: String) {
        _recentToolIds.update { current ->
            (listOf(toolId) + current.filterNot { it == toolId }).take(6)
        }
    }

    fun addHistoryEntry(
        expression: String,
        result: String,
        toolName: String,
        reusableRoute: String,
    ) {
        val entry = HistoryEntry(
            id = UUID.randomUUID().toString(),
            expression = expression,
            result = result,
            toolName = toolName,
            timeLabel = "Just now",
            group = HistoryGroup.TODAY,
            reusableRoute = reusableRoute,
        )
        _history.update { listOf(entry) + it }
        if (toolName == "Standard" || toolName == "Scientific") {
            _lastPreviewExpression.value = expression
            _lastPreviewResult.value = result
        }
    }

    fun deleteHistoryEntry(id: String) {
        _history.update { list -> list.filterNot { it.id == id } }
    }

    fun clearAllHistory() {
        _history.value = emptyList()
    }

    fun setReusedExpression(expression: String?) {
        _reusedExpression.value = expression
    }

    fun getRecentTools(): List<ToolItem> {
        val map = SampleCatalogs.allTools.associateBy { it.id }
        return _recentToolIds.value.mapNotNull { map[it] }
    }
}

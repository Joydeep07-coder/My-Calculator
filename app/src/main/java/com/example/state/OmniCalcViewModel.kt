package com.example.state

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SampleCatalogs
import com.example.data.local.DataStoreSettingsRepository
import com.example.data.local.HistoryRepository
import com.example.data.local.InMemoryHistoryRepository
import com.example.data.local.InMemorySettingsRepository
import com.example.data.local.OmniCalcDatabase
import com.example.data.local.RoomHistoryRepository
import com.example.data.local.SettingsRepository
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.HistoryEntry
import com.example.model.ThemeMode
import com.example.model.ToolItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OmniCalcViewModel(
    private val historyRepository: HistoryRepository = InMemoryHistoryRepository(),
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository(),
) : ViewModel() {

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

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { loaded ->
                _settings.value = loaded
            }
        }
        viewModelScope.launch {
            settingsRepository.onboardingCompleted.collect { completed ->
                _onboardingCompleted.value = completed
            }
        }
        viewModelScope.launch {
            settingsRepository.recentToolIds.collect { ids ->
                _recentToolIds.value = ids
            }
        }
        viewModelScope.launch {
            historyRepository.historyEntries.collect { entries ->
                _history.value = entries
                val latestCalc = entries.firstOrNull {
                    it.toolName == "Standard" || it.toolName == "Scientific"
                }
                if (latestCalc != null) {
                    _lastPreviewExpression.value = latestCalc.expression
                    _lastPreviewResult.value = latestCalc.result
                } else if (_lastPreviewExpression.value.isNotEmpty() && entries.isEmpty()) {
                    _lastPreviewExpression.value = ""
                    _lastPreviewResult.value = "0"
                }
            }
        }
    }

    private fun updateAndPersistSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        viewModelScope.launch {
            settingsRepository.saveSettings(updated)
        }
    }

    fun markOnboardingCompleted() {
        _onboardingCompleted.value = true
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        updateAndPersistSettings { it.copy(themeMode = mode) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        updateAndPersistSettings { it.copy(hapticFeedback = enabled) }
    }

    fun setButtonSound(enabled: Boolean) {
        updateAndPersistSettings { it.copy(buttonSound = enabled) }
    }

    fun setDecimalPrecision(precision: Int) {
        val clamped = precision.coerceIn(2, 10)
        updateAndPersistSettings { it.copy(decimalPrecision = clamped) }
    }

    fun setThousandsSeparator(enabled: Boolean) {
        updateAndPersistSettings { it.copy(thousandsSeparator = enabled) }
    }

    fun setAngleMode(mode: AngleMode) {
        updateAndPersistSettings { it.copy(angleMode = mode) }
    }

    fun setLanguage(language: String) {
        updateAndPersistSettings { it.copy(language = language) }
    }

    fun setCurrencyCode(code: String) {
        updateAndPersistSettings { it.copy(currencyCode = code.uppercase()) }
    }

    fun resetSettings() {
        val defaults = AppSettings()
        _settings.value = defaults
        viewModelScope.launch {
            settingsRepository.resetSettings()
        }
    }

    fun markToolUsed(toolId: String) {
        val updated = (listOf(toolId) + _recentToolIds.value.filterNot { it == toolId }).take(6)
        _recentToolIds.value = updated
        viewModelScope.launch {
            settingsRepository.setRecentToolIds(updated)
        }
    }

    fun addHistoryEntry(
        expression: String,
        result: String,
        toolName: String,
        reusableRoute: String,
    ) {
        val cleanExpr = expression.trim()
        val cleanResult = result.trim()
        val cleanTool = toolName.trim()
        if (cleanTool == "Standard" || cleanTool == "Scientific") {
            if (cleanExpr.isNotEmpty() && cleanResult.isNotEmpty() && cleanResult != "Error") {
                _lastPreviewExpression.value = cleanExpr
                _lastPreviewResult.value = cleanResult
            }
        }
        viewModelScope.launch {
            historyRepository.addEntry(
                expression = cleanExpr,
                result = cleanResult,
                toolName = cleanTool,
                reusableRoute = reusableRoute,
            )
        }
    }

    fun deleteHistoryEntry(id: String) {
        viewModelScope.launch {
            historyRepository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        _lastPreviewExpression.value = ""
        _lastPreviewResult.value = "0"
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun setReusedExpression(expression: String?) {
        _reusedExpression.value = expression
    }

    fun getRecentTools(): List<ToolItem> {
        val map = SampleCatalogs.allTools.associateBy { it.id }
        return _recentToolIds.value.mapNotNull { map[it] }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = OmniCalcDatabase.getInstance(appContext)
                    val historyRepo = RoomHistoryRepository(database.historyDao())
                    val settingsRepo = DataStoreSettingsRepository(appContext)
                    return OmniCalcViewModel(
                        historyRepository = historyRepo,
                        settingsRepository = settingsRepo,
                    ) as T
                }
            }
        }
    }
}

package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

private val Context.omniCalcDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "omni_calc_settings"
)

interface SettingsRepository {
    val settings: Flow<AppSettings>
    val onboardingCompleted: Flow<Boolean>
    val recentToolIds: Flow<List<String>>

    suspend fun saveSettings(settings: AppSettings)
    suspend fun resetSettings(): AppSettings
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setRecentToolIds(ids: List<String>)
}

class DataStoreSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    private val dataStore: DataStore<Preferences>
        get() = context.applicationContext.omniCalcDataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val BUTTON_SOUND = booleanPreferencesKey("button_sound")
        val DECIMAL_PRECISION = intPreferencesKey("decimal_precision")
        val THOUSANDS_SEPARATOR = booleanPreferencesKey("thousands_separator")
        val ANGLE_MODE = stringPreferencesKey("angle_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val RECENT_TOOL_IDS = stringPreferencesKey("recent_tool_ids")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    override val settings: Flow<AppSettings> = safeData.map { prefs ->
        val defaults = AppSettings()
        val theme = prefs[Keys.THEME_MODE]?.let { raw ->
            runCatching { ThemeMode.valueOf(raw) }.getOrNull()
        } ?: defaults.themeMode
        val angle = prefs[Keys.ANGLE_MODE]?.let { raw ->
            runCatching { AngleMode.valueOf(raw) }.getOrNull()
        } ?: defaults.angleMode
        AppSettings(
            themeMode = theme,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: defaults.hapticFeedback,
            buttonSound = prefs[Keys.BUTTON_SOUND] ?: defaults.buttonSound,
            decimalPrecision = (prefs[Keys.DECIMAL_PRECISION] ?: defaults.decimalPrecision).coerceIn(2, 10),
            thousandsSeparator = prefs[Keys.THOUSANDS_SEPARATOR] ?: defaults.thousandsSeparator,
            angleMode = angle,
            language = prefs[Keys.LANGUAGE] ?: defaults.language,
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: defaults.currencyCode,
        )
    }

    override val onboardingCompleted: Flow<Boolean> = safeData.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    override val recentToolIds: Flow<List<String>> = safeData.map { prefs ->
        prefs[Keys.RECENT_TOOL_IDS]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    override suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            writeSettings(prefs, settings)
        }
    }

    override suspend fun resetSettings(): AppSettings {
        val defaults = AppSettings()
        dataStore.edit { prefs ->
            writeSettings(prefs, defaults)
        }
        return defaults
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setRecentToolIds(ids: List<String>) {
        dataStore.edit { prefs ->
            prefs[Keys.RECENT_TOOL_IDS] = ids.joinToString(",")
        }
    }

    private fun writeSettings(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        settings: AppSettings,
    ) {
        prefs[Keys.THEME_MODE] = settings.themeMode.name
        prefs[Keys.HAPTIC_FEEDBACK] = settings.hapticFeedback
        prefs[Keys.BUTTON_SOUND] = settings.buttonSound
        prefs[Keys.DECIMAL_PRECISION] = settings.decimalPrecision.coerceIn(2, 10)
        prefs[Keys.THOUSANDS_SEPARATOR] = settings.thousandsSeparator
        prefs[Keys.ANGLE_MODE] = settings.angleMode.name
        prefs[Keys.LANGUAGE] = settings.language
        prefs[Keys.CURRENCY_CODE] = settings.currencyCode.uppercase()
    }
}

class InMemorySettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = _settings.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    override val onboardingCompleted: Flow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _recentToolIds = MutableStateFlow<List<String>>(emptyList())
    override val recentToolIds: Flow<List<String>> = _recentToolIds.asStateFlow()

    override suspend fun saveSettings(settings: AppSettings) {
        _settings.value = settings
    }

    override suspend fun resetSettings(): AppSettings {
        val defaults = AppSettings()
        _settings.value = defaults
        return defaults
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }

    override suspend fun setRecentToolIds(ids: List<String>) {
        _recentToolIds.value = ids
    }
}

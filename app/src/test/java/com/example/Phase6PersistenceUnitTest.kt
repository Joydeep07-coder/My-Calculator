package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DataStoreSettingsRepository
import com.example.data.local.HistoryEntity
import com.example.data.local.HistoryValidation
import com.example.data.local.OmniCalcDatabase
import com.example.data.local.RoomHistoryRepository
import com.example.model.AngleMode
import com.example.model.AppSettings
import com.example.model.HistoryGroup
import com.example.model.ThemeMode
import com.example.navigation.AppRoutes
import com.example.state.OmniCalcViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase6PersistenceUnitTest {

    private lateinit var context: Context
    private lateinit var db: OmniCalcDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        db = Room.databaseBuilder(
            context,
            OmniCalcDatabase::class.java,
            "test_omni_calc_db_${System.nanoTime()}",
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `history persists expressions results timestamps and tool types across restart`() = runBlocking {
        val repo1 = RoomHistoryRepository(db.historyDao())
        val settingsRepo1 = DataStoreSettingsRepository(context)
        val vm1 = OmniCalcViewModel(historyRepository = repo1, settingsRepository = settingsRepo1)

        vm1.addHistoryEntry("12×8", "96", "Standard", AppRoutes.STANDARD_CALCULATOR)
        vm1.addHistoryEntry("1000 + 18% GST", "₹1,180", "GST", AppRoutes.GST_CALCULATOR)

        val savedBeforeRestart = repo1.historyEntries.first { it.size == 2 }
        assertEquals("GST", savedBeforeRestart[0].toolName)
        assertEquals("Standard", savedBeforeRestart[1].toolName)
        assertTrue(savedBeforeRestart[0].timestampMillis > 0L)

        // Simulate app restart with new repository & ViewModel instances against same DB
        val repo2 = RoomHistoryRepository(db.historyDao())
        val vm2 = OmniCalcViewModel(
            historyRepository = repo2,
            settingsRepository = DataStoreSettingsRepository(context),
        )

        val restored = vm2.history.first { it.size == 2 }
        assertEquals(2, restored.size)
        assertEquals("1000 + 18% GST", restored[0].expression)
        assertEquals("₹1,180", restored[0].result)
        assertEquals("12×8", restored[1].expression)
        assertEquals("96", restored[1].result)
        assertEquals("12×8", vm2.lastPreviewExpression.value)
        assertEquals("96", vm2.lastPreviewResult.value)
    }

    @Test
    fun `history individual deletion and clear all persist across restart`() = runBlocking {
        val repo1 = RoomHistoryRepository(db.historyDao())
        val vm1 = OmniCalcViewModel(
            historyRepository = repo1,
            settingsRepository = DataStoreSettingsRepository(context),
        )

        vm1.addHistoryEntry("5+5", "10", "Standard", AppRoutes.STANDARD_CALCULATOR)
        vm1.addHistoryEntry("sin(30)", "0.5", "Scientific", AppRoutes.SCIENTIFIC_CALCULATOR)
        val initial = vm1.history.first { it.size == 2 }
        val firstId = initial.first { it.expression == "5+5" }.id

        // Delete one entry
        vm1.deleteHistoryEntry(firstId)
        val afterDelete = vm1.history.first { it.size == 1 }
        assertEquals("sin(30)", afterDelete.first().expression)

        // Verify persisted across restart
        val vm2 = OmniCalcViewModel(
            historyRepository = RoomHistoryRepository(db.historyDao()),
            settingsRepository = DataStoreSettingsRepository(context),
        )
        val restoredAfterDelete = vm2.history.first { it.size == 1 }
        assertEquals("sin(30)", restoredAfterDelete.first().expression)

        // Clear all
        vm2.clearAllHistory()
        val afterClear = db.historyDao().observeAll().first { it.isEmpty() }
        assertTrue(afterClear.isEmpty())
    }

    @Test
    fun `history rejects invalid entries and prevents duplicates`() = runBlocking {
        val repo = RoomHistoryRepository(db.historyDao())

        assertFalse(repo.addEntry("", "10", "Standard", AppRoutes.STANDARD_CALCULATOR))
        assertFalse(repo.addEntry("5÷0", "Error", "Standard", AppRoutes.STANDARD_CALCULATOR))
        assertFalse(repo.addEntry("x", "NaN", "Standard", AppRoutes.STANDARD_CALCULATOR))
        assertFalse(repo.addEntry("1+1", "—", "GST", AppRoutes.GST_CALCULATOR))

        assertTrue(repo.addEntry("2+2", "4", "Standard", AppRoutes.STANDARD_CALCULATOR, timestampMillis = 1_000L))
        assertTrue(repo.addEntry("2+2", "4", "Standard", AppRoutes.STANDARD_CALCULATOR, timestampMillis = 2_000L))

        val list = repo.historyEntries.first { it.isNotEmpty() }
        assertEquals(1, list.size)
        assertEquals(2_000L, list.first().timestampMillis)

        // Verify timestamp grouping across Today / Yesterday / Earlier
        val now = 1_700_000_000_000L
        assertEquals(HistoryGroup.TODAY, HistoryValidation.groupForTimestamp(now, now))
        assertEquals(HistoryGroup.YESTERDAY, HistoryValidation.groupForTimestamp(now - 24 * 60 * 60 * 1000L, now))
        assertEquals(HistoryGroup.EARLIER, HistoryValidation.groupForTimestamp(now - 3 * 24 * 60 * 60 * 1000L, now))
    }

    @Test
    fun `all AppSettings values persist across restart and resetSettings restores defaults`() = runBlocking {
        val settingsRepo1 = DataStoreSettingsRepository(context)
        val vm1 = OmniCalcViewModel(
            historyRepository = RoomHistoryRepository(db.historyDao()),
            settingsRepository = settingsRepo1,
        )

        vm1.setThemeMode(ThemeMode.LIGHT)
        vm1.setCurrencyCode("USD")
        vm1.setDecimalPrecision(3)
        vm1.setThousandsSeparator(false)
        vm1.setAngleMode(AngleMode.RAD)
        vm1.setHapticFeedback(false)
        vm1.setButtonSound(true)
        vm1.setLanguage("Japanese")
        vm1.markOnboardingCompleted()
        vm1.markToolUsed("gst_calc")

        val persistedSnapshot = settingsRepo1.settings.first {
            it.themeMode == ThemeMode.LIGHT &&
                it.currencyCode == "USD" &&
                it.decimalPrecision == 3 &&
                !it.thousandsSeparator &&
                it.angleMode == AngleMode.RAD &&
                !it.hapticFeedback &&
                it.buttonSound &&
                it.language == "Japanese"
        }
        assertEquals(ThemeMode.LIGHT, persistedSnapshot.themeMode)

        // Simulate app restart with fresh DataStoreSettingsRepository and ViewModel
        val settingsRepo2 = DataStoreSettingsRepository(context)
        val vm2 = OmniCalcViewModel(
            historyRepository = RoomHistoryRepository(db.historyDao()),
            settingsRepository = settingsRepo2,
        )

        val restored = vm2.settings.first { it.currencyCode == "USD" && it.themeMode == ThemeMode.LIGHT }
        assertEquals(ThemeMode.LIGHT, restored.themeMode)
        assertEquals("USD", restored.currencyCode)
        assertEquals(3, restored.decimalPrecision)
        assertFalse(restored.thousandsSeparator)
        assertEquals(AngleMode.RAD, restored.angleMode)
        assertFalse(restored.hapticFeedback)
        assertTrue(restored.buttonSound)
        assertEquals("Japanese", restored.language)
        assertTrue(vm2.onboardingCompleted.first { it })
        assertEquals(listOf("gst_calc"), vm2.recentToolIds.first { it.isNotEmpty() })

        // Reset settings and confirm defaults persist
        vm2.resetSettings()
        val afterReset = settingsRepo2.settings.first { it == AppSettings() }
        assertEquals(AppSettings(), afterReset)
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleCatalogs
import com.example.data.SimpleExprHelper
import com.example.model.UnitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Calculator", appName)
    }

    @Test
    fun `expression evaluator and unit converter work`() {
        val sum = SimpleExprHelper.evaluate("(10 + 5) * 2")
        assertNotNull(sum)
        assertEquals(30.0, sum!!, 1e-6)

        val tempUnits = SampleCatalogs.unitDefinitions[UnitCategory.TEMPERATURE]!!
        val c = tempUnits.first { it.id == "c" }
        val f = tempUnits.first { it.id == "f" }
        val converted = SampleCatalogs.convertUnit(UnitCategory.TEMPERATURE, c, f, 100.0)
        assertEquals(212.0, converted, 1e-6)
    }
}

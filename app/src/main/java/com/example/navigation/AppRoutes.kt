package com.example.navigation

object AppRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // Bottom Navigation Tabs
    const val HOME = "home"
    const val CALCULATOR = "calculator"
    const val TOOLS = "tools"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    // Feature Screens
    const val STANDARD_CALCULATOR = "standard_calculator"
    const val SCIENTIFIC_CALCULATOR = "scientific_calculator"
    const val GST_CALCULATOR = "gst_calculator"
    const val FINANCE_HUB = "finance_hub"
    const val FINANCE_EMI = "finance_emi"
    const val FINANCE_SIMPLE_INTEREST = "finance_simple_interest"
    const val FINANCE_COMPOUND_INTEREST = "finance_compound_interest"
    const val FINANCE_DISCOUNT = "finance_discount"
    const val UNIT_CONVERTER = "unit_converter"
    const val UNIT_CONVERTER_WITH_CAT = "unit_converter/{category}"
    const val CURRENCY_CONVERTER = "currency_converter"
    const val AGE_CALCULATOR = "age_calculator"
    const val BMI_CALCULATOR = "bmi_calculator"
    const val PROGRAMMER_CALCULATOR = "programmer_calculator"
    const val GRAPH_PLOTTER = "graph_plotter"
    const val ABOUT = "about"

    fun unitConverterRoute(categoryName: String = "LENGTH"): String = "unit_converter/$categoryName"

    val bottomNavRoutes = setOf(HOME, CALCULATOR, TOOLS, HISTORY, SETTINGS)
}

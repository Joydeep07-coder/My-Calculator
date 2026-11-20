package com.example.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.state.OmniCalcViewModel
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AgeCalculatorScreen
import com.example.ui.screens.BmiCalculatorScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CompoundInterestScreen
import com.example.ui.screens.CurrencyConverterScreen
import com.example.ui.screens.DiscountCalculatorScreen
import com.example.ui.screens.EmiCalculatorScreen
import com.example.ui.screens.FinanceHubScreen
import com.example.ui.screens.GraphPlotterScreen
import com.example.ui.screens.GstCalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProgrammerCalculatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimpleInterestScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.screens.UnitConverterScreen

@Composable
fun OmniCalcApp(viewModel: OmniCalcViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showMainNav = currentRoute in AppRoutes.bottomNavRoutes

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToScreen(route: String) {
        if (route in AppRoutes.bottomNavRoutes) {
            navigateToTab(route)
        } else {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            bottomBar = {
                if (showMainNav && !useNavRail) {
                    OmniBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = ::navigateToTab,
                    )
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showMainNav && !useNavRail) innerPadding.calculateBottomPadding() else 0.dp),
            ) {
                if (showMainNav && useNavRail) {
                    OmniNavigationRail(
                        currentRoute = currentRoute,
                        onNavigate = ::navigateToTab,
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.SPLASH,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    composable(AppRoutes.SPLASH) {
                        SplashScreen(
                            onFinished = {
                                val nextRoute = if (viewModel.onboardingCompleted.value) {
                                    AppRoutes.HOME
                                } else {
                                    AppRoutes.ONBOARDING
                                }
                                navController.navigate(nextRoute) {
                                    popUpTo(AppRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoutes.ONBOARDING) {
                        OnboardingScreen(
                            onComplete = {
                                viewModel.markOnboardingCompleted()
                                navController.navigate(AppRoutes.HOME) {
                                    popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoutes.HOME) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigate = ::navigateToScreen,
                            onOpenTool = { tool ->
                                viewModel.markToolUsed(tool.id)
                                navigateToScreen(tool.route)
                            },
                        )
                    }

                    composable(AppRoutes.CALCULATOR) {
                        CalculatorScreen(
                            viewModel = viewModel,
                            initialScientific = false,
                            onBack = null,
                            onOpenHistory = { navigateToTab(AppRoutes.HISTORY) },
                        )
                    }

                    composable(AppRoutes.STANDARD_CALCULATOR) {
                        CalculatorScreen(
                            viewModel = viewModel,
                            initialScientific = false,
                            onBack = { navController.popBackStack() },
                            onOpenHistory = { navigateToTab(AppRoutes.HISTORY) },
                        )
                    }

                    composable(AppRoutes.SCIENTIFIC_CALCULATOR) {
                        CalculatorScreen(
                            viewModel = viewModel,
                            initialScientific = true,
                            onBack = { navController.popBackStack() },
                            onOpenHistory = { navigateToTab(AppRoutes.HISTORY) },
                        )
                    }

                    composable(AppRoutes.TOOLS) {
                        ToolsHubScreen(
                            onOpenTool = { tool ->
                                viewModel.markToolUsed(tool.id)
                                navigateToScreen(tool.route)
                            },
                        )
                    }

                    composable(AppRoutes.HISTORY) {
                        HistoryScreen(
                            viewModel = viewModel,
                            onReuseEntry = { entry ->
                                val target = if (entry.toolName == "Scientific") {
                                    viewModel.setReusedExpression(entry.expression)
                                    AppRoutes.SCIENTIFIC_CALCULATOR
                                } else if (entry.toolName == "Standard") {
                                    viewModel.setReusedExpression(entry.expression)
                                    AppRoutes.CALCULATOR
                                } else {
                                    entry.reusableRoute
                                }
                                navigateToScreen(target)
                            },
                        )
                    }

                    composable(AppRoutes.SETTINGS) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigate = ::navigateToScreen,
                            onReplayOnboarding = {
                                navController.navigate(AppRoutes.ONBOARDING)
                            },
                        )
                    }

                    composable(AppRoutes.GST_CALCULATOR) {
                        GstCalculatorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.FINANCE_HUB) {
                        FinanceHubScreen(
                            onNavigate = ::navigateToScreen,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.FINANCE_EMI) {
                        EmiCalculatorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.FINANCE_SIMPLE_INTEREST) {
                        SimpleInterestScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.FINANCE_COMPOUND_INTEREST) {
                        CompoundInterestScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.FINANCE_DISCOUNT) {
                        DiscountCalculatorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.UNIT_CONVERTER) {
                        UnitConverterScreen(
                            initialCategoryName = "LENGTH",
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = AppRoutes.UNIT_CONVERTER_WITH_CAT,
                        arguments = listOf(navArgument("category") { type = NavType.StringType }),
                    ) { entry ->
                        val cat = entry.arguments?.getString("category") ?: "LENGTH"
                        UnitConverterScreen(
                            initialCategoryName = cat,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.CURRENCY_CONVERTER) {
                        CurrencyConverterScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel,
                        )
                    }

                    composable(AppRoutes.AGE_CALCULATOR) {
                        AgeCalculatorScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.BMI_CALCULATOR) {
                        BmiCalculatorScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(AppRoutes.PROGRAMMER_CALCULATOR) {
                        ProgrammerCalculatorScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel,
                        )
                    }

                    composable(AppRoutes.GRAPH_PLOTTER) {
                        GraphPlotterScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel,
                        )
                    }

                    composable(AppRoutes.ABOUT) {
                        AboutScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}

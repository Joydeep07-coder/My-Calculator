package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navigation.OmniCalcApp
import com.example.state.OmniCalcViewModel
import com.example.ui.theme.OmniCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val omniViewModel: OmniCalcViewModel = viewModel()
            val settings by omniViewModel.settings.collectAsStateWithLifecycle()

            OmniCalcTheme(themeMode = settings.themeMode) {
                OmniCalcApp(viewModel = omniViewModel)
            }
        }
    }
}

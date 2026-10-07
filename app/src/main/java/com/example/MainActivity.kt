package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.ChittiApp
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            val themeMode = when (settings?.themeMode) {
                "DARK" -> ThemeMode.DARK
                "LIGHT" -> ThemeMode.LIGHT
                else -> ThemeMode.SYSTEM
            }

            MyApplicationTheme(
                themeMode = themeMode,
                onThemeModeChange = { newMode ->
                    viewModel.setThemeMode(newMode.name)
                }
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChittiApp(viewModel = viewModel)
                }
            }
        }
    }
}

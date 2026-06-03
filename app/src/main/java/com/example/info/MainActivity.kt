package com.example.info

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.info.ui.theme.INFOTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "zh") ?: "zh"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        setPreferredRefreshRate()
        
        setContent {
            var themeMode by remember { 
                mutableStateOf(sharedPreferences.getString("theme_mode", "System") ?: "System") 
            }
            var language by remember {
                mutableStateOf(sharedPreferences.getString("language", "zh") ?: "zh")
            }

            val darkTheme = when (themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            INFOTheme(darkTheme = darkTheme) {
                MainScreen(
                    context = this,
                    currentTheme = themeMode,
                    onThemeChange = { newMode ->
                        themeMode = newMode
                        sharedPreferences.edit().putString("theme_mode", newMode).apply()
                    },
                    currentLanguage = language,
                    onLanguageChange = { newLang ->
                        if (language != newLang) {
                            language = newLang
                            sharedPreferences.edit().putString("language", newLang).apply()
                            recreate() // 重启以应用语言
                        }
                    }
                )
            }
        }
    }
    
    private fun setPreferredRefreshRate() {
        val display = windowManager.defaultDisplay
        val supportedModes = display.supportedModes
        
        if (supportedModes.isNotEmpty()) {
            val maxRefreshRateMode = supportedModes.maxByOrNull { it.refreshRate }
            maxRefreshRateMode?.let {
                window.attributes.preferredDisplayModeId = it.modeId
            }
        }
    }
}

@Composable
fun MainScreen(
    context: Context,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(screen.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (Screen.entries[selectedTabIndex]) {
            Screen.Home -> HomeScreen(context, Modifier.padding(innerPadding))
            Screen.Environment -> EnvironmentScreen(Modifier.padding(innerPadding))
            Screen.Restart -> RestartScreen(context)
            Screen.Settings -> SettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }
    }
}

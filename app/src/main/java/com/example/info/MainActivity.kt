package com.example.info

import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.info.ui.theme.INFOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setPreferredRefreshRate()
        
        setContent {
            INFOTheme {
                MainScreen(this)
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
fun MainScreen(context: Context) {
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
            Screen.Restart -> RestartScreen(context)
            Screen.Empty -> EmptyScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    INFOTheme {
        EmptyScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenDarkPreview() {
    INFOTheme(darkTheme = true) {
        EmptyScreen()
    }
}

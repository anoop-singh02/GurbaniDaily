package com.anoop.gurbanidaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.anoop.gurbanidaily.navigation.AppNavigation
import com.anoop.gurbanidaily.ui.theme.GurbaniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = (application as GurbaniApp).prefs
        setContent {
            val dark by prefs.darkMode.collectAsState(initial = false)
            val dynamic by prefs.dynamicColor.collectAsState(initial = true)
            GurbaniTheme(darkTheme = dark, dynamicColor = dynamic) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

package com.anoop.gurbanidaily

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = remember { getSharedPreferences("gurbani_prefs", Context.MODE_PRIVATE) }
            var dark by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
            GurbaniTheme(darkTheme = dark) {
                MainScreen(
                    darkMode = dark,
                    onToggleDark = {
                        dark = it
                        prefs.edit().putBoolean("dark_mode", it).apply()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(darkMode: Boolean, onToggleDark: (Boolean) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var shabad by remember { mutableStateOf(ShabadPicker.shabadForToday()) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Gurbani", fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = shabad.gurmukhi,
                        fontSize = 24.sp,
                        lineHeight = 38.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = shabad.transliteration,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(Modifier.fillMaxWidth(0.4f))
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = shabad.meaning,
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = shabad.source,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = { shabad = ShabadPicker.randomShabad() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Shuffle")
                }
                OutlinedButton(onClick = {
                    val text = "${shabad.gurmukhi}\n\n${shabad.transliteration}\n\n${shabad.meaning}\n\n— ${shabad.source}"
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, "Share Shabad"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { shabad = ShabadPicker.shabadForToday() }) {
                Text("Back to today's shabad")
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            darkMode = darkMode,
            onToggleDark = onToggleDark,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun SettingsDialog(darkMode: Boolean, onToggleDark: (Boolean) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Settings") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark mode")
                    Switch(checked = darkMode, onCheckedChange = onToggleDark)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Gurbani Daily — ${GurbaniData.shabads.size} shabads, offline. " +
                        "Built with love. Waheguru Ji Ka Khalsa, Waheguru Ji Ki Fateh.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

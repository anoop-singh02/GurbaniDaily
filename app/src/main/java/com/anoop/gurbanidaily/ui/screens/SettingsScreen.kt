package com.anoop.gurbanidaily.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.GurbaniData
import com.anoop.gurbanidaily.notifications.ReminderScheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()

    val dark by prefs.darkMode.collectAsState(initial = false)
    val dynamic by prefs.dynamicColor.collectAsState(initial = true)
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)
    val reminderOn by prefs.reminderEnabled.collectAsState(initial = false)
    val hour by prefs.reminderHour.collectAsState(initial = 6)
    val minute by prefs.reminderMinute.collectAsState(initial = 0)

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                prefs.setReminderEnabled(true)
                ReminderScheduler.schedule(context, hour, minute)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard("Appearance") {
                SwitchRow("Dark mode", dark) {
                    scope.launch { prefs.setDarkMode(it) }
                }
                SwitchRow(
                    title = "Use phone wallpaper colours (Material You)",
                    checked = dynamic,
                    subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) null
                    else "Requires Android 12 or later"
                ) {
                    scope.launch { prefs.setDynamicColor(it) }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Gurmukhi font size: ${"%.1f".format(fontScale)}×",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = fontScale,
                    onValueChange = { scope.launch { prefs.setFontScale(it) } },
                    valueRange = 0.8f..1.6f,
                    steps = 7
                )
            }

            SectionCard("Daily reminder") {
                SwitchRow(
                    title = "Notify me every day",
                    checked = reminderOn,
                    subtitle = "Tap the notification to open today's shabad"
                ) { wantOn ->
                    if (wantOn) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            scope.launch {
                                prefs.setReminderEnabled(true)
                                ReminderScheduler.schedule(context, hour, minute)
                            }
                        }
                    } else {
                        scope.launch {
                            prefs.setReminderEnabled(false)
                            ReminderScheduler.cancel(context)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time", fontWeight = FontWeight.Medium)
                    TextButton(onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                scope.launch {
                                    prefs.setReminderTime(h, m)
                                    if (reminderOn) {
                                        ReminderScheduler.schedule(context, h, m)
                                    }
                                }
                            },
                            hour, minute, false
                        ).show()
                    }) {
                        Text("%02d:%02d".format(hour, minute))
                    }
                }
            }

            SectionCard("About") {
                Text(
                    "Daily Gurbani — ${GurbaniData.shabads.size} shabads bundled, fully offline.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Waheguru Ji Ka Khalsa, Waheguru Ji Ki Fateh.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

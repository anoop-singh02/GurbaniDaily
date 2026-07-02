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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import com.anoop.gurbanidaily.ui.components.GradientBackdrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.BuildConfig
import com.anoop.gurbanidaily.data.AutoUpdater
import com.anoop.gurbanidaily.data.Backup
import com.anoop.gurbanidaily.data.ReminderSlot
import com.anoop.gurbanidaily.data.UpdateInfo
import com.anoop.gurbanidaily.notifications.ReminderScheduler
import kotlinx.coroutines.flow.first
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

    // Update check state
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }

    // Backup status
    var backupStatus by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                Backup.exportTo(context, uri, prefs).fold(
                    onSuccess = { backupStatus = "Exported your data." },
                    onFailure = { backupStatus = "Export failed: ${it.localizedMessage}" }
                )
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                Backup.importFrom(context, uri, prefs).fold(
                    onSuccess = { stats ->
                        backupStatus = "Imported ${stats.favorites} favourites, ${stats.journalEntries} journal entries."
                    },
                    onFailure = { backupStatus = "Import failed: ${it.localizedMessage}" }
                )
            }
        }
    }

    var pendingSlot by remember { mutableStateOf<ReminderSlot?>(null) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val slot = pendingSlot
        if (granted && slot != null) {
            scope.launch {
                prefs.setReminderEnabled(slot, true)
                val state = prefs.reminder(slot).first()
                ReminderScheduler.schedule(context, slot, state.hour, state.minute)
            }
        }
        pendingSlot = null
    }

    GradientBackdrop(darkTheme = isSystemInDarkTheme()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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

            SectionCard("Daily reminders") {
                Text(
                    "Tap a notification to open today's shabad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ReminderSlot.entries.forEachIndexed { idx, slot ->
                    val state by prefs.reminder(slot).collectAsState(
                        initial = com.anoop.gurbanidaily.data.ReminderState(
                            false, slot.defaultHour, slot.defaultMinute
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(slot.label, fontWeight = FontWeight.Medium)
                            TextButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, h, m ->
                                            scope.launch {
                                                prefs.setReminderTime(slot, h, m)
                                                if (state.enabled) {
                                                    ReminderScheduler.schedule(context, slot, h, m)
                                                }
                                            }
                                        },
                                        state.hour, state.minute, false
                                    ).show()
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("%02d:%02d".format(state.hour, state.minute))
                            }
                        }
                        Switch(
                            checked = state.enabled,
                            onCheckedChange = { wantOn ->
                                if (wantOn) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pendingSlot = slot
                                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        scope.launch {
                                            prefs.setReminderEnabled(slot, true)
                                            ReminderScheduler.schedule(
                                                context, slot, state.hour, state.minute
                                            )
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        prefs.setReminderEnabled(slot, false)
                                        ReminderScheduler.cancel(context, slot)
                                    }
                                }
                            }
                        )
                    }
                    if (idx < ReminderSlot.entries.lastIndex) HorizontalDivider()
                }
            }

            SectionCard("Updates") {
                Text(
                    "Installed: build ${BuildConfig.VERSION_CODE} · v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                checkingUpdate = true
                                updateStatus = null
                                AutoUpdater.checkForUpdate().fold(
                                    onSuccess = { info ->
                                        if (info == null) updateStatus = "You're on the latest build."
                                        else updateInfo = info
                                    },
                                    onFailure = { updateStatus = "Couldn't check: ${it.localizedMessage}" }
                                )
                                prefs.setLastUpdateCheck(System.currentTimeMillis())
                                checkingUpdate = false
                            }
                        },
                        enabled = !checkingUpdate
                    ) {
                        if (checkingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.padding(4.dp))
                        }
                        Text("Check for updates")
                    }
                }
                val info = updateInfo
                if (info != null) {
                    Text(
                        "Update available: ${info.tagName} (build ${info.buildNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val prog = downloadProgress
                    if (prog != null) {
                        Text(
                            "Downloading… ${(prog * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = {
                        scope.launch {
                            downloadProgress = 0f
                            AutoUpdater.downloadApk(context, info) { p ->
                                downloadProgress = p
                            }.fold(
                                onSuccess = { file ->
                                    downloadProgress = null
                                    AutoUpdater.launchInstaller(context, file)
                                },
                                onFailure = {
                                    downloadProgress = null
                                    updateStatus = "Download failed: ${it.localizedMessage}"
                                }
                            )
                        }
                    }) { Text("Download & install") }
                }
                if (updateStatus != null) {
                    Text(
                        updateStatus!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SectionCard("Backup") {
                Text(
                    "Save your favourites, history, and reflections to a file you control. Restore on a new phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = {
                        exportLauncher.launch("gurbani-daily-backup.json")
                    }) { Text("Export") }
                    FilledTonalButton(onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/plain"))
                    }) { Text("Import") }
                }
                if (backupStatus != null) {
                    Text(
                        backupStatus!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SectionCard("About") {
                Text(
                    "Daily Gurbani — full Sri Guru Granth Sahib Ji via BaniDB.",
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

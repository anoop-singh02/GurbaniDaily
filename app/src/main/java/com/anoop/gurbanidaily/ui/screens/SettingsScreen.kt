package com.anoop.gurbanidaily.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.anoop.gurbanidaily.BuildConfig
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.AutoUpdater
import com.anoop.gurbanidaily.data.Backup
import com.anoop.gurbanidaily.data.NanakshahiCalendar
import com.anoop.gurbanidaily.data.ReminderSlot
import com.anoop.gurbanidaily.data.ReminderState
import com.anoop.gurbanidaily.data.UpdateInfo
import com.anoop.gurbanidaily.notifications.ReminderScheduler
import com.anoop.gurbanidaily.notifications.SangrandScheduler
import com.anoop.gurbanidaily.ui.components.GradientBackdrop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenChangelog: () -> Unit = {}) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()

    val dark by prefs.darkMode.collectAsState(initial = false)
    val dynamic by prefs.dynamicColor.collectAsState(initial = true)
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)

    var checkingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
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
                        backupStatus =
                            "Imported ${stats.favorites} favourites, ${stats.journalEntries} journal entries."
                    },
                    onFailure = { backupStatus = "Import failed: ${it.localizedMessage}" }
                )
            }
        }
    }

    var pendingSlot by remember { mutableStateOf<ReminderSlot?>(null) }
    var notificationsAllowed by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = granted
        if (granted) {
            SangrandScheduler.scheduleDaily(context)
        }
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

    fun requestNotificationPermissionFor(slot: ReminderSlot?) {
        pendingSlot = slot
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                SettingsHeader()

                SettingsGroup(
                    title = "Reading",
                    subtitle = "Control the look and Gurbani text size.",
                    icon = Icons.Outlined.Palette
                ) {
                    SwitchSettingRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark mode",
                        subtitle = "Use the dark reading theme.",
                        checked = dark,
                        onChange = { scope.launch { prefs.setDarkMode(it) } }
                    )
                    GroupDivider()
                    SwitchSettingRow(
                        icon = Icons.Outlined.Wallpaper,
                        title = "Wallpaper colours",
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            "Match supported Android wallpaper colours."
                        } else {
                            "Requires Android 12 or later."
                        },
                        checked = dynamic,
                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                        onChange = { scope.launch { prefs.setDynamicColor(it) } }
                    )
                    GroupDivider()
                    FontScaleRow(
                        value = fontScale,
                        onChange = { scope.launch { prefs.setFontScale(it) } }
                    )
                }

                SettingsGroup(
                    title = "Reminders",
                    subtitle = "Daily notifications open today's shabad.",
                    icon = Icons.Outlined.NotificationsActive
                ) {
                    ReminderSlot.entries.forEachIndexed { idx, slot ->
                        val state by prefs.reminder(slot).collectAsState(
                            initial = ReminderState(false, slot.defaultHour, slot.defaultMinute)
                        )
                        ReminderSettingRow(
                            slot = slot,
                            state = state,
                            onPickTime = {
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
                                    state.hour,
                                    state.minute,
                                    false
                                ).show()
                            },
                            onToggle = { wantOn ->
                                if (wantOn) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        requestNotificationPermissionFor(slot)
                                    } else {
                                        scope.launch {
                                            prefs.setReminderEnabled(slot, true)
                                            ReminderScheduler.schedule(
                                                context,
                                                slot,
                                                state.hour,
                                                state.minute
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
                        if (idx < ReminderSlot.entries.lastIndex) GroupDivider()
                    }
                }

                SettingsGroup(
                    title = "Punjabi Months",
                    subtitle = "Sangrand reminders for the Nanakshahi calendar.",
                    icon = Icons.Outlined.CalendarMonth
                ) {
                    InfoSettingRow(
                        icon = Icons.Outlined.EventRepeat,
                        title = "Monthly Sangrand",
                        subtitle = "Notifies around 8 AM on the first day of each Punjabi month."
                    )
                    GroupDivider()
                    PermissionStatusRow(
                        allowed = notificationsAllowed,
                        onAllow = { requestNotificationPermissionFor(null) }
                    )
                    GroupDivider()
                    Text(
                        NanakshahiCalendar.months.joinToString(" · ") { it.english },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SettingsGroup(
                    title = "Updates",
                    subtitle = "Build ${BuildConfig.VERSION_CODE} · v${BuildConfig.VERSION_NAME}",
                    icon = Icons.Outlined.SystemUpdate
                ) {
                    UpdateStatusRow(updateInfo, updateStatus, downloadProgress)
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onOpenChangelog,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("What's new", maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    checkingUpdate = true
                                    updateStatus = null
                                    updateInfo = null
                                    AutoUpdater.checkForUpdate().fold(
                                        onSuccess = { info ->
                                            updateInfo = info
                                            updateStatus = if (info == null) {
                                                "You're on the latest build."
                                            } else {
                                                null
                                            }
                                        },
                                        onFailure = {
                                            updateStatus = "Couldn't check: ${it.localizedMessage}"
                                        }
                                    )
                                    prefs.setLastUpdateCheck(System.currentTimeMillis())
                                    checkingUpdate = false
                                }
                            },
                            enabled = !checkingUpdate,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (checkingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Check", maxLines = 1)
                        }
                    }
                    val info = updateInfo
                    if (info != null) {
                        FilledTonalButton(
                            onClick = {
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
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download & install")
                        }
                    }
                }

                SettingsGroup(
                    title = "Backup",
                    subtitle = "Move favourites, history, and reflections between phones.",
                    icon = Icons.Outlined.Backup
                ) {
                    InfoSettingRow(
                        icon = Icons.Outlined.Verified,
                        title = "Your file, your control",
                        subtitle = "Export a JSON backup and restore it later from the system picker."
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { exportLauncher.launch("gurbani-daily-backup.json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Export", maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Import", maxLines = 1)
                        }
                    }
                    if (backupStatus != null) {
                        StatusText(backupStatus!!)
                    }
                }

                SettingsGroup(
                    title = "About",
                    subtitle = "Daily Gurbani",
                    icon = Icons.Outlined.Info
                ) {
                    Text(
                        "Online Sri Guru Granth Sahib Ji through BaniDB, with daily reflections, Hukamnama Sahib, favourites, history, and reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Waheguru Ji Ka Khalsa, Waheguru Ji Ki Fateh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun SettingsHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Daily Gurbani",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Keep the app tuned for daily reflection, reminders, updates, and your saved shabads.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingBadge("Build ${BuildConfig.VERSION_CODE} · v${BuildConfig.VERSION_NAME}")
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(icon = icon, title = title, subtitle = subtitle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon = icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon = icon, compact = true)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked && enabled,
            onCheckedChange = onChange,
            enabled = enabled
        )
    }
}

@Composable
private fun FontScaleRow(value: Float, onChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        IconBubble(icon = Icons.Outlined.TextFields, compact = true)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Gurmukhi size",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                SettingBadge("${"%.1f".format(value)}x")
            }
            Text(
                "Adjust Gurbani text in the daily reflection and reader.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = 0.8f..1.6f,
                steps = 7
            )
        }
    }
}

@Composable
private fun ReminderSettingRow(
    slot: ReminderSlot,
    state: ReminderState,
    onPickTime: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon = Icons.Outlined.Schedule, compact = true)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                slot.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            TextButton(
                onClick = onPickTime,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.heightIn(min = 32.dp)
            ) {
                Text("%02d:%02d".format(state.hour, state.minute))
            }
        }
        Switch(checked = state.enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun InfoSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon = icon, compact = true)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionStatusRow(allowed: Boolean, onAllow: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(
            icon = if (allowed) Icons.Outlined.Verified else Icons.Outlined.NotificationsActive,
            compact = true
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (allowed) "Notifications allowed" else "Notification permission needed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                if (allowed) {
                    "Daily and Sangrand reminders can be delivered."
                } else {
                    "Allow notifications so Punjabi month reminders can fire."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!allowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            FilledTonalButton(onClick = onAllow) {
                Text("Allow")
            }
        }
    }
}

@Composable
private fun UpdateStatusRow(
    info: UpdateInfo?,
    status: String?,
    downloadProgress: Float?
) {
    val title = when {
        downloadProgress != null -> "Downloading update"
        info != null -> "Update available"
        status != null -> status
        else -> "Installed build"
    }
    val subtitle = when {
        downloadProgress != null -> "Downloading ${(downloadProgress * 100).toInt()}%"
        info != null -> "${info.displayName} · build ${info.buildNumber}"
        else -> "Build ${BuildConfig.VERSION_CODE} · v${BuildConfig.VERSION_NAME}"
    }
    InfoSettingRow(
        icon = if (info != null) Icons.Outlined.Download else Icons.Outlined.SystemUpdate,
        title = title,
        subtitle = subtitle
    )
}

@Composable
private fun IconBubble(icon: ImageVector, compact: Boolean = false) {
    val size = if (compact) 36.dp else 42.dp
    val iconSize = if (compact) 19.dp else 22.dp
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun SettingBadge(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                shape = CircleShape
            )
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
private fun StatusText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start
    )
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
    )
}

package com.anoop.gurbanidaily.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.anoop.gurbanidaily.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val tagName: String,
    val buildNumber: Int,
    val downloadUrl: String,
    val sizeBytes: Long
)

object AutoUpdater {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/anoop-singh02/GurbaniDaily/releases/latest"

    suspend fun checkForUpdate(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = httpGet(LATEST_RELEASE_URL)
            val root = JSONObject(raw)
            val tag = root.optString("tag_name", "")
            val build = tag.removePrefix("build-").toIntOrNull() ?: return@runCatching null
            val assets = root.optJSONArray("assets") ?: return@runCatching null
            var downloadUrl = ""
            var size = 0L
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val name = a.optString("name", "")
                if (name.endsWith(".apk")) {
                    downloadUrl = a.optString("browser_download_url", "")
                    size = a.optLong("size", 0L)
                    break
                }
            }
            if (downloadUrl.isBlank()) return@runCatching null
            if (build <= BuildConfig.VERSION_CODE) return@runCatching null
            UpdateInfo(tag, build, downloadUrl, size)
        }
    }

    suspend fun downloadApk(
        context: Context,
        info: UpdateInfo,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "updates").apply { mkdirs() }
            val out = File(dir, "GurbaniDaily-${info.tagName}.apk")
            val conn = (URL(info.downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 30_000
                setRequestProperty("User-Agent", "GurbaniDaily/${BuildConfig.VERSION_NAME}")
            }
            val total = if (info.sizeBytes > 0) info.sizeBytes else conn.contentLengthLong
            conn.inputStream.use { input ->
                out.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var read: Int
                    var done = 0L
                    while (input.read(buf).also { read = it } > 0) {
                        output.write(buf, 0, read)
                        done += read
                        if (total > 0) onProgress(done.toFloat() / total.toFloat())
                    }
                }
            }
            out
        }
    }

    fun launchInstaller(context: Context, apk: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settings = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settings)
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "com.anoop.gurbanidaily.updates",
            apk
        )
        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(install)
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "GurbaniDaily/${BuildConfig.VERSION_NAME}")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}

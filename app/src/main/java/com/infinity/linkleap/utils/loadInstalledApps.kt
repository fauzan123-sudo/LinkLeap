package com.infinity.linkleap.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.infinity.linkleap.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun loadInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val result = mutableListOf<AppInfo>()

    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    for (app in packages) {
        // hanya aplikasi yang punya launcher intent
        val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            val name = pm.getApplicationLabel(app).toString()
            val icon = pm.getApplicationIcon(app.packageName)
            val apkPath = app.sourceDir ?: continue
            result.add(
                AppInfo(
                    name = name,
                    packageName = app.packageName,
                    icon = icon,
                    apkPath = apkPath
                )
            )
        }
    }

    // FIXED: return sorted list
    return@withContext result.sortedBy { it.name.lowercase() }
}

fun categorizeApps(apps: List<AppInfo>, context: Context): Map<String, List<AppInfo>> {
    val pm = context.packageManager
    val currentTime = System.currentTimeMillis()
    val oneWeekMillis = 7L * 24 * 60 * 60 * 1000

    // Kategori "Baru" - terinstal dalam 7 hari terakhir
    val newApps = apps.filter {
        try {
            val info = pm.getPackageInfo(it.packageName, 0)
            currentTime - info.firstInstallTime <= oneWeekMillis
        } catch (e: Exception) {
            false
        }
    }

    // Kategori "Widget" - nama mengandung kata "widget"
    val widgets = apps.filter {
        it.name.contains("widget", ignoreCase = true)
    }

    // Kategori "Aplikasi" - semua aplikasi normal (bukan baru & bukan widget)
    val regularApps = apps.filterNot { app ->
        app in newApps || app in widgets
    }

    // Kategori "Lainnya" - untuk system apps atau apps tanpa kategori jelas
    // Opsional: bisa diisi dengan system apps atau dibiarkan kosong
    val systemApps = apps.filter { app ->
        try {
            val appInfo = pm.getApplicationInfo(app.packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    app !in newApps && app !in widgets && app !in regularApps
        } catch (e: Exception) {
            false
        }
    }

    return mapOf(
        "Baru" to newApps,
        "Aplikasi" to regularApps,
        "Lainnya" to systemApps,
        "Widget" to widgets
    )
}

fun findLocalApkFiles(context: Context): List<File> {
    val apkFiles = mutableListOf<File>()

    // Opsi 1: Public Download folder (perlu READ_EXTERNAL_STORAGE permission)
//    try {
//        val publicDownload = Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_DOWNLOADS
//        )
//        if (publicDownload.exists()) {
//            publicDownload.walkTopDown()
//                .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
//                .forEach { apkFiles.add(it) }
//        }
//    } catch (e: Exception) {
//        Log.e("APK_SCAN", "Error scanning public download: ${e.message}")
//    }

    // Opsi 2: App-specific external storage (tidak perlu permission di Android 10+)
//    try {
//        val appExternalDirs = ContextCompat.getExternalFilesDirs(context, null)
//        appExternalDirs.forEach { dir ->
//            dir?.walkTopDown()
//                ?.filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
//                ?.forEach { apkFiles.add(it) }
//        }
//    } catch (e: Exception) {
//        Log.e("APK_SCAN", "Error scanning app external: ${e.message}")
//    }

    // Opsi 3: Scan internal storage root (perlu MANAGE_EXTERNAL_STORAGE di Android 11+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
            try {
                val externalRoot = Environment.getExternalStorageDirectory()
                externalRoot.walkTopDown()
                    .maxDepth(3) // batasi kedalaman scan untuk performa
                    .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
                    .forEach { apkFiles.add(it) }
            } catch (e: Exception) {
                Log.e("APK_SCAN", "Error scanning external root: ${e.message}")
            }
        }
    }

    return apkFiles.distinctBy { it.absolutePath } // hapus duplikat
}

package me.ash.reader.ui.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import me.ash.reader.R
import me.ash.reader.data.model.general.Version
import me.ash.reader.data.model.general.toVersion
import java.io.File

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.getCurrentVersion(): Version = packageManager
    .getPackageInfo(packageName, 0)
    .versionName
    .toVersion()

fun Context.getLatestApk(): File = File(cacheDir, "latest.apk")

fun Context.getFileProvider(): String = "${packageName}.fileprovider"

fun Context.installLatestApk() {
    try {
        val contentUri = FileProvider.getUriForFile(this, getFileProvider(), getLatestApk())
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setDataAndType(contentUri, "application/vnd.android.package-archive")
        }
        if (packageManager.queryIntentActivities(intent, 0).size > 0) {
            startActivity(intent)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.e("RLog", "installLatestApk: ${e.message}")
    }
}

private var toast: Toast? = null

fun Context.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    toast?.cancel()
    toast = Toast.makeText(this, message, duration)
    toast?.show()
}

fun Context.showToastLong(message: String?) {
    showToast(message, Toast.LENGTH_LONG)
}

fun Context.share(content: String) {
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        putExtra(
            Intent.EXTRA_TEXT,
            content,
        )
        type = "text/plain"
    }, getString(R.string.share)))
}

fun Context.openURL(url: String?) {
    url?.takeIf { it.trim().isNotEmpty() }
        ?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
}

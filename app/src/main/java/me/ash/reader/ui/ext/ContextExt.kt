package me.ash.reader.ui.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import me.ash.reader.data.entity.Version

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.getCurrentVersion(): Version =
    Version(packageManager.getPackageInfo(packageName, 0).versionName)
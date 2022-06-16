package me.ash.reader

import android.content.Context
import android.os.Looper
import android.util.Log
import me.ash.reader.ui.ext.showToastLong
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

/**
 * The uncaught exception handler for the application.
 */
class CrashHandler(private val context: Context) : UncaughtExceptionHandler {

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * Catch all uncaught exception and log it.
     */
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        Looper.myLooper() ?: Looper.prepare()
        context.showToastLong(p1.message)
        Looper.loop()
        p1.printStackTrace()
        Log.e("RLog", "uncaughtException: ${p1.message}")
        android.os.Process.killProcess(android.os.Process.myPid());
        exitProcess(1)
    }
}

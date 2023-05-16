package me.ash.reader

import android.content.Context
import android.os.Looper
import android.util.Log
import me.ash.reader.ui.ext.showToastLong
import java.lang.Thread.UncaughtExceptionHandler

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
        val causeMessage = getCauseMessage(p1)
        Log.e("RLog", "uncaughtException: $causeMessage")
        Looper.myLooper() ?: Looper.prepare()
        context.showToastLong(causeMessage)
        Looper.loop()
        p1.printStackTrace()
        // android.os.Process.killProcess(android.os.Process.myPid());
        // exitProcess(1)
    }

    private fun getCauseMessage(e: Throwable?): String? {
        val cause = getCauseRecursively(e)
        return if (cause != null) cause.message.toString() else e?.javaClass?.name
    }

    private fun getCauseRecursively(e: Throwable?): Throwable? {
        var cause: Throwable?
        cause = e
        while (cause?.cause != null && cause !is RuntimeException) {
            cause = cause.cause
        }
        return cause
    }
}

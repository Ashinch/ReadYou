package me.ash.reader.ui.ext

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.os.ConfigurationCompat
import me.ash.reader.R
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@SuppressLint("SimpleDateFormat")
object DateFormat {
    val YYYY_MM_DD_HH_MM_SS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val YYYY_MM_DD_DASH_HH_MM_SS = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
}

fun Date.toString(format: SimpleDateFormat): String {
    return format.format(this)
}

fun Date.formatAsString(
    context: Context,
    onlyHourMinute: Boolean? = false,
    atHourMinute: Boolean? = false,
): String {
    val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
    val df = DateFormat.getDateInstance(DateFormat.FULL, locale)
    return when {
        onlyHourMinute == true -> {
            SimpleDateFormat("HH:mm", locale).format(this)
        }

        atHourMinute == true -> {
            context.getString(
                R.string.date_at_time,
                df.format(this),
                SimpleDateFormat("HH:mm", locale).format(this),
            )
        }

        else -> {
            df.format(this).run {
                when (this) {
                    df.format(Date()) -> context.getString(R.string.today)
                    df.format(
                        Calendar.getInstance().apply {
                            time = Date()
                            add(Calendar.DAY_OF_MONTH, -1)
                        }.time
                    ),
                    -> context.getString(R.string.yesterday)

                    else -> this
                }
            }
        }
    }
}

private fun String.parseToDate(
    patterns: Array<String> = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss",
        "yyyyMMdd",
        "yyyy/MM/dd",
        "yyyy年MM月dd日",
        "yyyy MM dd",
    ),
): Date? {
    val df = SimpleDateFormat()
    for (pattern in patterns) {
        df.applyPattern(pattern)
        df.isLenient = false
        val date = df.parse(this, ParsePosition(0))
        if (date != null) {
            return date
        }
    }
    return null
}

package me.ash.reader

import android.content.Context
import androidx.core.os.ConfigurationCompat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.formatToString(
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
                    ) -> context.getString(R.string.yesterday)
                    else -> this
                }
            }
        }
    }
}
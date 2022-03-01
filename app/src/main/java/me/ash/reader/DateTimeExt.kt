package me.ash.reader

import java.text.SimpleDateFormat
import java.util.*

object DateTimeExt {

    const val HH_MM_SS = "HH:mm:ss"
    const val HH_MM = "HH:mm"
    const val MM_SS = "mm:ss"
    const val YYYY_MM_DD_HH_MM_SS = "yyyy年MM月dd日 HH:mm:ss"
    const val YYYY_MM_DD_HH_MM = "yyyy年MM月dd日 HH:mm"
    const val YYYY_MM_DD = "yyyy年MM月dd日"
    const val YYYY_MM = "yyyy年MM月"
    const val YYYY = "yyyy年"
    const val MM = "MM月"
    const val DD = "dd日"

    /**
     * Returns a date-time [String] format from a [Date] object.
     */
    fun Date.toString(pattern: String, simpleDate: Boolean? = false): String {
        return if (simpleDate == true) {
            val format = if (pattern == YYYY_MM_DD) {
                ""
            } else {
                SimpleDateFormat(
                    pattern.replace(YYYY_MM_DD, "")
                ).format(this)
            }
            when (this.toString(YYYY_MM_DD)) {
                Date().toString(YYYY_MM_DD) -> {
                    "今天${format}"
                }
                Calendar.getInstance().apply {
                    time = Date()
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time.toString(YYYY_MM_DD) -> {
                    "昨天${format}"
                }
                else -> SimpleDateFormat(pattern).format(this)
            }
        } else {
            SimpleDateFormat(pattern).format(this)
        }
    }

    /**
     * Returns a [Date] object parsed from a date-time [String].
     */
    fun String.toDate(pattern: String? = null): Date =
        SimpleDateFormat((pattern ?: YYYY_MM_DD_HH_MM_SS)).parse(this)
}
package me.ash.reader.data.model.general

import java.util.*

enum class MarkAsReadConditions {
    SevenDays,
    ThreeDays,
    OneDay,
    All,
    ;

    fun toDate(): Date? = when (this) {
        All -> null
        else -> Calendar.getInstance().apply {
            time = Date()
            add(Calendar.DAY_OF_MONTH, when (this@MarkAsReadConditions) {
                SevenDays -> -7
                ThreeDays -> -3
                OneDay -> -1
                else -> throw IllegalArgumentException("Unknown condition: $this")
            })
        }.time
    }
}

package me.ash.reader.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.ui.ext.formatAsString
import java.util.*
import javax.inject.Inject

class StringsRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun getString(resId: Int) = context.getString(resId)
    fun formatAsString(date: Date?) = date?.formatAsString(context)
}

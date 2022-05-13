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
    fun getString(resId: Int, vararg formatArgs: Any) = context.getString(resId, *formatArgs)
    fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any) = context.resources.getQuantityString(resId, quantity, *formatArgs)
    fun formatAsString(date: Date?) = date?.formatAsString(context)
}

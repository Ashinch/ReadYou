package me.ash.reader.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StringsRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun getString(resId: Int) = context.getString(resId)
}

package me.ash.reader.data.preference

import android.content.Context
import kotlinx.coroutines.CoroutineScope

sealed class Preference {
    abstract fun put(context: Context, scope: CoroutineScope)
}
package me.ash.reader.data.preference

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ArticleListDatePreference(val value: Boolean) : Preference() {
    object ON : ArticleListDatePreference(true)
    object OFF : ArticleListDatePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.ArticleListDate,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.articleListDate: Flow<ArticleListDatePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.ArticleListDate.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun ArticleListDatePreference.not(): ArticleListDatePreference =
    when (value) {
        true -> ArticleListDatePreference.OFF
        false -> ArticleListDatePreference.ON
    }
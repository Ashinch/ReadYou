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

sealed class ArticleListFeedNamePreference(val value: Boolean) : Preference() {
    object ON : ArticleListFeedNamePreference(true)
    object OFF : ArticleListFeedNamePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.ArticleListFeedName,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.articleListFeedName: Flow<ArticleListFeedNamePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.ArticleListFeedName.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun ArticleListFeedNamePreference.not(): ArticleListFeedNamePreference =
    when (value) {
        true -> ArticleListFeedNamePreference.OFF
        false -> ArticleListFeedNamePreference.ON
    }
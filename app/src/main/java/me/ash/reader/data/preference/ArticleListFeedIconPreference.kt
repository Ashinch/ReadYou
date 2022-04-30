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

sealed class ArticleListFeedIconPreference(val value: Boolean) : Preference() {
    object ON : ArticleListFeedIconPreference(true)
    object OFF : ArticleListFeedIconPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.ArticleListFeedIcon,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.articleListFeedIcon: Flow<ArticleListFeedIconPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.ArticleListFeedIcon.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun ArticleListFeedIconPreference.not(): ArticleListFeedIconPreference =
    when (value) {
        true -> ArticleListFeedIconPreference.OFF
        false -> ArticleListFeedIconPreference.ON
    }
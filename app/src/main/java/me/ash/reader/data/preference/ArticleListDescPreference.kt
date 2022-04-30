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

sealed class ArticleListDescPreference(val value: Boolean) : Preference() {
    object ON : ArticleListDescPreference(true)
    object OFF : ArticleListDescPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.ArticleListDesc,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.articleListDesc: Flow<ArticleListDescPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.ArticleListDesc.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun ArticleListDescPreference.not(): ArticleListDescPreference =
    when (value) {
        true -> ArticleListDescPreference.OFF
        false -> ArticleListDescPreference.ON
    }
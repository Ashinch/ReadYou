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

sealed class ArticleListImagePreference(val value: Boolean) : Preference() {
    object ON : ArticleListImagePreference(true)
    object OFF : ArticleListImagePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.ArticleListImage,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.articleListImage: Flow<ArticleListImagePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.ArticleListImage.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun ArticleListImagePreference.not(): ArticleListImagePreference =
    when (value) {
        true -> ArticleListImagePreference.OFF
        false -> ArticleListImagePreference.ON
    }
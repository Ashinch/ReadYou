package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListReadIndicator
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowArticleListReadIndicator =
    compositionLocalOf<FlowArticleReadIndicatorPreference> { FlowArticleReadIndicatorPreference.default }

sealed class FlowArticleReadIndicatorPreference(val value: Boolean) : Preference() {
    object ExcludingStarred : FlowArticleReadIndicatorPreference(true)
    object AllRead : FlowArticleReadIndicatorPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowArticleListReadIndicator,
                value
            )
        }
    }

    val description: String
        @Composable get() {
            return when (this) {
                AllRead -> stringResource(id = R.string.all_read)
                ExcludingStarred -> stringResource(id = R.string.read_excluding_starred)
            }
        }

    companion object {

        val default = ExcludingStarred
        val values = listOf(ExcludingStarred, AllRead)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListReadIndicator]?.key as Preferences.Key<Boolean>]) {
                true -> ExcludingStarred
                false -> AllRead
                else -> default
            }

    }
}

operator fun FlowArticleReadIndicatorPreference.not(): FlowArticleReadIndicatorPreference =
    when (value) {
        true -> FlowArticleReadIndicatorPreference.AllRead
        false -> FlowArticleReadIndicatorPreference.ExcludingStarred
    }

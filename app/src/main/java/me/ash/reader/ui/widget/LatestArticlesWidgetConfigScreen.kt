package me.ash.reader.ui.widget

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.widget.LocalShowFeedIcon
import me.ash.reader.infrastructure.preference.widget.LocalShowFeedName
import me.ash.reader.infrastructure.preference.widget.ShowFeedNamePreference
import me.ash.reader.infrastructure.preference.widget.latestArticleWidgetSettings
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.get
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight


@Composable
fun LatestArticlesWidgetConfigScreen(onSave: () -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = context.latestArticleWidgetSettings
    val showFeedIcon = LocalShowFeedIcon.current
    val showFeedName = LocalShowFeedName.current

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.widget_settings), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.show_feed_icon),
                        onClick = { showFeedIcon.toggle(context, scope) }
                    ) {
                        RYSwitch(activated = showFeedIcon.value) {
                            showFeedIcon.toggle(context, scope)
                            Log.d("ConfigScreen", "New value: ${showFeedIcon.value}")
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.show_feed_name),
                        onClick = { showFeedName.toggle(context, scope) }
                    ) {
                        RYSwitch(activated = showFeedName.value) {
                            showFeedName.toggle(context, scope)
                            Log.d("ConfigScreen", "dataStore: ${context.dataStore.get<Boolean>("showFeedName")}")
                        }
                    }
                }

            }
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            settings.load(context)
            onSave()
        }) {
            Text("Save")
        }
    }
}
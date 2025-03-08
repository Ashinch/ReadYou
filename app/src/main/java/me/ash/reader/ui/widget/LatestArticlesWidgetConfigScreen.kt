package me.ash.reader.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight


@Composable
fun LatestArticlesWidgetConfigScreen(
    appWidgetId: Int,
    widgetPreferencesManager: WidgetPreferencesManager,
    onSave: () -> Unit
) {

    val scope = rememberCoroutineScope()
    val showFeedIcon = widgetPreferencesManager.showFeedIcon
    val showFeedIconState by showFeedIcon.asFlow(appWidgetId).collectAsState(initial = showFeedIcon.default)
    val showFeedName = widgetPreferencesManager.showFeedName
    val showFeedNameState by showFeedName.asFlow(appWidgetId).collectAsState(initial = showFeedName.default)

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
                        onClick = {
                            scope.launch {
                                showFeedIcon.toggle(appWidgetId)
                            }
                        }
                    ) {
                        RYSwitch(activated = showFeedIconState) {
                            scope.launch {
                                showFeedIcon.toggle(appWidgetId)
                            }

                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.show_feed_name),
                        onClick = {
                            scope.launch {
                                showFeedName.toggle(appWidgetId)
                            }
                        }
                    ) {
                        RYSwitch(activated = showFeedNameState) {
                            scope.launch {
                                showFeedName.toggle(appWidgetId)
                            }

                        }
                    }
                }

            }
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = onSave) {
                    Text("Save")
                }
            }
        }
    )


}
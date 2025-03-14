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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ash.reader.R
import me.ash.reader.domain.model.group.Group
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.preference.widget.ReadArticleDisplayOption
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

private val ReadArticleDisplayOption.descString @Composable get() =
    stringResource(
        when (this) {
            ReadArticleDisplayOption.SHOW -> R.string.show_articles
            ReadArticleDisplayOption.SOFT_HIDE -> R.string.grey_out_articles
            ReadArticleDisplayOption.HARD_HIDE -> R.string.hide_articles
        }
    )

@Composable
fun LatestArticlesWidgetConfigScreen(
    appWidgetId: Int,
    widgetPreferencesManager: WidgetPreferencesManager,
    onSave: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Preference state
    val headingText = widgetPreferencesManager.headingText
    val headingTextState = headingText.asFlow(appWidgetId)
        .collectAsState(initial = headingText.default)
    var headingTextValue: String? by remember {
        mutableStateOf(headingText.getCachedOrDefault(appWidgetId))
    }
    val groupToDisplay = widgetPreferencesManager.groupToDisplay
    val groupToDisplayState = groupToDisplay.asFlow(appWidgetId)
        .collectAsState(initial = groupToDisplay.default)
    val maxLatestArticleCount = widgetPreferencesManager.maxLatestArticleCount
    val maxLatestArticleCountState = maxLatestArticleCount.asFlow(appWidgetId)
        .collectAsState(initial = maxLatestArticleCount.default)
    var maxLatestArticleCountValue: Int? by remember {
        mutableStateOf(maxLatestArticleCount.getCachedOrDefault(appWidgetId))
    }
    val showFeedIcon = widgetPreferencesManager.showFeedIcon
    val showFeedIconState by showFeedIcon.asFlow(appWidgetId)
        .collectAsState(initial = showFeedIcon.default)
    val showFeedName = widgetPreferencesManager.showFeedName
    val showFeedNameState by showFeedName.asFlow(appWidgetId)
        .collectAsState(initial = showFeedName.default)
    val readArticleDisplay = widgetPreferencesManager.readArticleDisplay
    val readArticleDisplayState = readArticleDisplay.asFlow(appWidgetId)
        .collectAsState(initial = readArticleDisplay.default)

    // Data for dialogs
    var headingTextDialogVisible by remember { mutableStateOf(false) }

    var groupToDisplayDialogVisible by remember { mutableStateOf(false) }
    val groupDao = AndroidDatabase.getInstance(context).groupDao()
    val groups = runBlocking {
        groupDao.queryAll(context.currentAccountId)
    } + Group("", stringResource(R.string.none), context.currentAccountId)
    val groupIdToName = groups.associate {
        it.id to it.name
    }

    var maxLatestArticleCountDialogVisible by remember { mutableStateOf(false) }
    var readArticleDisplayDialogVisible by remember { mutableStateOf(false) }

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
                        title = stringResource(R.string.heading_text),
                        desc = headingTextState.value,
                        onClick = { headingTextDialogVisible = true }
                    )
                    SettingItem(
                        title = stringResource(R.string.group_to_display),
                        desc = groupIdToName[groupToDisplayState.value],
                        onClick = { groupToDisplayDialogVisible = true }
                    )
                    SettingItem(
                        title = stringResource(R.string.max_latest_article_count),
                        desc = "${maxLatestArticleCountState.value}",
                        onClick = { maxLatestArticleCountDialogVisible = true }
                    )
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
                    SettingItem(
                        title = stringResource(R.string.what_to_do_with_read_articles),
                        desc = ReadArticleDisplayOption.valueOf(readArticleDisplayState.value).descString,
                        onClick = { readArticleDisplayDialogVisible = true }
                    )
                }

            }
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = onSave) {
                    Text("Save")
                }
            }
        }
    )

    TextFieldDialog(
        visible = headingTextDialogVisible,
        title = stringResource(R.string.heading_text),
        value = headingTextValue ?: "",
        placeholder = stringResource(R.string.value),
        onDismissRequest = { headingTextDialogVisible = false },
        onValueChange = {
            headingTextValue = it
        },
        onConfirm = {
            scope.launch {
                headingTextValue?.let {
                    headingText.put(appWidgetId, it)
                }
                headingTextDialogVisible = false
            }
        }
    )

    RadioDialog(
        visible = groupToDisplayDialogVisible,
        title = stringResource(R.string.group_to_display),
        options = groups.map {
            RadioDialogOption(
                text = it.name,
                selected = it.id == groupToDisplayState.value
            ) {
                runBlocking {
                    groupToDisplay.put(appWidgetId, it.id)
                }
            }
        }
    ) {
        groupToDisplayDialogVisible = false
    }

    TextFieldDialog(
        visible = maxLatestArticleCountDialogVisible,
        title = stringResource(R.string.max_latest_article_count),
        value = (maxLatestArticleCountValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            maxLatestArticleCountValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            maxLatestArticleCountValue = maxLatestArticleCount.getCached(appWidgetId)
            maxLatestArticleCountDialogVisible = false
        },
        onConfirm = {
            scope.launch {
                maxLatestArticleCountValue?.let {
                    maxLatestArticleCount.put(appWidgetId, it)
                }
                maxLatestArticleCountDialogVisible = false
            }

        }
    )

    RadioDialog(
        visible = readArticleDisplayDialogVisible,
        title = stringResource(R.string.what_to_do_with_read_articles),
        options = ReadArticleDisplayOption.entries.map {
            RadioDialogOption(
                text = it.descString,
                selected = readArticleDisplayState.value == it.name
            ) {
                runBlocking {
                    readArticleDisplay.putOption(appWidgetId, it)
                }
            }
        }
    ) {
        readArticleDisplayDialogVisible = false
    }


}
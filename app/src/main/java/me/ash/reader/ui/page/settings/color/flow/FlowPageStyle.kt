package me.ash.reader.ui.page.settings.color.flow

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.model.Filter
import me.ash.reader.data.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.page.home.flow.ArticleItem
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onDark
import me.ash.reader.ui.theme.palette.onLight
import java.util.*

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowPageStyle(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val filterBarStyle = LocalFlowFilterBarStyle.current
    val filterBarFilled = LocalFlowFilterBarFilled.current
    val filterBarPadding = LocalFlowFilterBarPadding.current
    val filterBarTonalElevation = LocalFlowFilterBarTonalElevation.current
    val topBarTonalElevation = LocalFlowTopBarTonalElevation.current
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListFeedName = LocalFlowArticleListFeedName.current
    val articleListImage = LocalFlowArticleListImage.current
    val articleListDesc = LocalFlowArticleListDesc.current
    val articleListTime = LocalFlowArticleListTime.current
    val articleListStickyDate = LocalFlowArticleListDateStickyHeader.current
    val articleListTonalElevation = LocalFlowArticleListTonalElevation.current

    val scope = rememberCoroutineScope()

    var filterBarStyleDialogVisible by remember { mutableStateOf(false) }
    var filterBarPaddingDialogVisible by remember { mutableStateOf(false) }
    var filterBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var topBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var articleListTonalElevationDialogVisible by remember { mutableStateOf(false) }

    var filterBarPaddingValue: Int? by remember { mutableStateOf(filterBarPadding) }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface
                ),
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        navController.popBackStack()
                    }
                },
                actions = {}
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.flow_page), desc = "")
                }

                // Preview
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface
                                        onLight MaterialTheme.colorScheme.surface.copy(0.7f)
                            )
                            .clickable { },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowPagePreview(
                            topBarTonalElevation = topBarTonalElevation,
                            articleListTonalElevation = articleListTonalElevation,
                            filterBarStyle = filterBarStyle.value,
                            filterBarFilled = filterBarFilled.value,
                            filterBarPadding = filterBarPadding.dp,
                            filterBarTonalElevation = filterBarTonalElevation.value.dp,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Top Bar
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.top_bar)
                    )
                    SettingItem(
                        title = stringResource(R.string.mark_as_read_button_position),
                        desc = stringResource(R.string.top),
                        enable = false,
                        onClick = {},
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${topBarTonalElevation.value}dp",
                        onClick = {
                            topBarTonalElevationDialogVisible = true
                        },
                    ) {}
//                    Tips(text = stringResource(R.string.tips_top_bar_tonal_elevation))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Article List
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.article_list)
                    )
                    SettingItem(
                        title = stringResource(R.string.feed_favicons),
                        onClick = {
                            (!articleListFeedIcon).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = articleListFeedIcon.value) {
                            (!articleListFeedIcon).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.feed_names),
                        onClick = {
                            (!articleListFeedName).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = articleListFeedName.value) {
                            (!articleListFeedName).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_images),
                        onClick = {
                            (!articleListImage).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = articleListImage.value) {
                            (!articleListImage).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_desc),
                        onClick = {
                            (!articleListDesc).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = articleListDesc.value) {
                            (!articleListDesc).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_date),
                        onClick = {
                            (!articleListTime).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = articleListTime.value) {
                            (!articleListTime).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_date_sticky_header),
                        onClick = {
                            (!articleListStickyDate).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = articleListStickyDate.value) {
                            (!articleListStickyDate).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${articleListTonalElevation.value}dp",
                        onClick = {
                            articleListTonalElevationDialogVisible = true
                        },
                    ) {}
                    Tips(text = stringResource(R.string.tips_article_list_tonal_elevation))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Filter Bar
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.filter_bar),
                    )
                    SettingItem(
                        title = stringResource(R.string.style),
                        desc = filterBarStyle.getDesc(context),
                        onClick = {
                            filterBarStyleDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.fill_selected_icon),
                        onClick = {
                            (!filterBarFilled).put(context, scope)
                        },
                    ) {
                        me.ash.reader.ui.component.base.Switch(activated = filterBarFilled.value) {
                            (!filterBarFilled).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.padding_on_both_ends),
                        desc = "${filterBarPadding}dp",
                        onClick = {
                            filterBarPaddingValue = filterBarPadding
                            filterBarPaddingDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${filterBarTonalElevation.value}dp",
                        onClick = {
                            filterBarTonalElevationDialogVisible = true
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    )

    RadioDialog(
        visible = filterBarStyleDialogVisible,
        title = stringResource(R.string.style),
        options = FlowFilterBarStylePreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == filterBarStyle,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        filterBarStyleDialogVisible = false
    }

    TextFieldDialog(
        visible = filterBarPaddingDialogVisible,
        title = stringResource(R.string.padding_on_both_ends),
        value = (filterBarPaddingValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            filterBarPaddingValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            filterBarPaddingDialogVisible = false
        },
        onConfirm = {
            FlowFilterBarPaddingPreference.put(context, scope, filterBarPaddingValue ?: 0)
            filterBarPaddingDialogVisible = false
        }
    )

    RadioDialog(
        visible = filterBarTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FlowFilterBarTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == filterBarTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        filterBarTonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = topBarTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FlowTopBarTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == topBarTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        topBarTonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = articleListTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FlowArticleListTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == articleListTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        articleListTonalElevationDialogVisible = false
    }
}

@Composable
fun FlowPagePreview(
    topBarTonalElevation: FlowTopBarTonalElevationPreference,
    articleListTonalElevation: FlowArticleListTonalElevationPreference,
    filterBarStyle: Int,
    filterBarFilled: Boolean,
    filterBarPadding: Dp,
    filterBarTonalElevation: Dp,
) {
    var filter by remember { mutableStateOf(Filter.Unread) }

    Column(
        modifier = Modifier
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    articleListTonalElevation.value.dp
                ) onDark MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        SmallTopAppBar(
            title = {},
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    topBarTonalElevation.value.dp
                ),
            ),
            navigationIcon = {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                ) {}
            },
            actions = {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = stringResource(R.string.mark_all_as_read),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {}
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {}
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ArticleItem(
            articleWithFeed = ArticleWithFeed(
                Article(
                    id = "",
                    title = stringResource(R.string.preview_article_title),
                    shortDescription = stringResource(R.string.preview_article_desc),
                    rawDescription = stringResource(R.string.preview_article_desc),
                    link = "",
                    feedId = "",
                    accountId = 0,
                    date = Date(),
                    isStarred = true,
                    img = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1yZWxhdGVkfDJ8fHxlbnwwfHx8fA%3D%3D&auto=format&fit=crop&w=800&q=60"
                ),
                feed = Feed(
                    id = "",
                    name = stringResource(R.string.preview_feed_name),
                    icon = "",
                    accountId = 0,
                    groupId = "",
                    url = "",
                ),
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterBar(
            modifier = Modifier.padding(horizontal = 12.dp),
            filter = filter,
            filterBarStyle = filterBarStyle,
            filterBarFilled = filterBarFilled,
            filterBarPadding = filterBarPadding,
            filterBarTonalElevation = filterBarTonalElevation,
        ) {
            filter = it
        }
    }
}
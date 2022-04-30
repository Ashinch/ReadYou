package me.ash.reader.ui.page.settings.color.flow

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Filter
import me.ash.reader.data.preference.*
import me.ash.reader.data.preference.ArticleListDatePreference.Companion.articleListDate
import me.ash.reader.data.preference.ArticleListDescPreference.Companion.articleListDesc
import me.ash.reader.data.preference.ArticleListFeedIconPreference.Companion.articleListFeedIcon
import me.ash.reader.data.preference.ArticleListFeedNamePreference.Companion.articleListFeedName
import me.ash.reader.data.preference.ArticleListImagePreference.Companion.articleListImage
import me.ash.reader.data.preference.ArticleListTonalElevationPreference.Companion.articleListTonalElevation
import me.ash.reader.data.preference.FilterBarFilledPreference.Companion.filterBarFilled
import me.ash.reader.data.preference.FilterBarPaddingPreference.filterBarPadding
import me.ash.reader.data.preference.FilterBarStylePreference.Companion.filterBarStyle
import me.ash.reader.data.preference.FilterBarTonalElevationPreference.Companion.filterBarTonalElevation
import me.ash.reader.ui.component.*
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.flow.ArticleItem
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight
import java.util.*

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowPageStyle(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val filterBarStyle =
        context.filterBarStyle.collectAsStateValue(initial = FilterBarStylePreference.default)
    val filterBarFilled =
        context.filterBarFilled.collectAsStateValue(initial = FilterBarFilledPreference.default)
    val filterBarPadding =
        context.filterBarPadding.collectAsStateValue(initial = FilterBarPaddingPreference.default)
    val filterBarTonalElevation =
        context.filterBarTonalElevation.collectAsStateValue(initial = FilterBarTonalElevationPreference.default)
    val articleListFeedIcon =
        context.articleListFeedIcon.collectAsStateValue(initial = ArticleListFeedIconPreference.default)
    val articleListFeedName =
        context.articleListFeedName.collectAsStateValue(initial = ArticleListFeedNamePreference.default)
    val articleListImage =
        context.articleListImage.collectAsStateValue(initial = ArticleListImagePreference.default)
    val articleListDesc =
        context.articleListDesc.collectAsStateValue(initial = ArticleListDescPreference.default)
    val articleListDate =
        context.articleListDate.collectAsStateValue(initial = ArticleListDatePreference.default)
    val articleListTonalElevation =
        context.articleListTonalElevation.collectAsStateValue(initial = ArticleListTonalElevationPreference.default)

    var filterBarStyleDialogVisible by remember { mutableStateOf(false) }
    var filterBarPaddingDialogVisible by remember { mutableStateOf(false) }
    var filterBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
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
                        FeedsPagePreview(articleListTonalElevation)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = "过滤栏",
                    )
                    SettingItem(
                        title = "样式",
                        desc = filterBarStyle.getDesc(context),
                        onClick = {
                            filterBarStyleDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = "填充已选中的图标",
                        onClick = {
                            (!filterBarFilled).put(context, scope)
                        },
                    ) {
                        Switch(activated = filterBarFilled.value) {
                            (!filterBarFilled).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = "两端边距",
                        desc = "${filterBarPadding}dp",
                        onClick = {
                            filterBarPaddingDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = "色调海拔",
                        desc = "${filterBarTonalElevation.value}dp",
                        onClick = {
                            filterBarTonalElevationDialogVisible = true
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = "标题栏"
                    )
                    SettingItem(
                        title = "“标记为已读”按钮的位置",
                        desc = "顶部",
                        onClick = {},
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = "文章列表"
                    )
                    SettingItem(
                        title = "显示订阅源图标",
                        onClick = {
                            (!articleListFeedIcon).put(context, scope)
                        },
                    ) {
                        Switch(activated = articleListFeedIcon.value) {
                            (!articleListFeedIcon).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = "显示订阅源名称",
                        onClick = {
                            (!articleListFeedName).put(context, scope)
                        },
                    ) {
                        Switch(activated = articleListFeedName.value) {
                            (!articleListFeedName).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = "显示文章插图",
                        onClick = {},
                    ) {
                        Switch(activated = false, enable = false)
                    }
                    SettingItem(
                        title = "显示文章描述",
                        onClick = {
                            (!articleListDesc).put(context, scope)
                        },
                    ) {
                        Switch(activated = articleListDesc.value) {
                            (!articleListDesc).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = "显示文章发布时间",
                        onClick = {
                            (!articleListDate).put(context, scope)
                        },
                    ) {
                        Switch(activated = articleListDate.value) {
                            (!articleListDate).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = "色调海拔",
                        desc = "${articleListTonalElevation.value}dp",
                        onClick = {
                            articleListTonalElevationDialogVisible = true
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    )

    RadioDialog(
        visible = filterBarStyleDialogVisible,
        title = stringResource(R.string.initial_filter),
        options = FilterBarStylePreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = filterBarStyle == it,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        filterBarStyleDialogVisible = false
    }

    TextFieldDialog(
        visible = filterBarPaddingDialogVisible,
        title = "两端边距",
        value = (filterBarPaddingValue ?: "").toString(),
        placeholder = stringResource(R.string.name),
        onValueChange = {
            filterBarPaddingValue = it.toIntOrNull()
        },
        onDismissRequest = {
            filterBarPaddingDialogVisible = false
        },
        onConfirm = {
            FilterBarPaddingPreference.put(context, scope, filterBarPaddingValue ?: 0)
            filterBarPaddingDialogVisible = false
        }
    )

    RadioDialog(
        visible = filterBarTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FilterBarTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = filterBarTonalElevation == it,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        filterBarTonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = articleListTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = ArticleListTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = articleListTonalElevation == it,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        articleListTonalElevationDialogVisible = false
    }
}

@Composable
fun FeedsPagePreview(
    articleListTonalElevation: ArticleListTonalElevationPreference,
) {
    var filter by remember { mutableStateOf(Filter.Unread) }

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(articleListTonalElevation.value.dp),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        ArticleItem(
            articleWithFeed = ArticleWithFeed(
                Article(
                    id = "",
                    title = "《黎明之剑》撒花完结，白金远瞳的“希灵三部曲”值得你通宵阅读",
                    shortDescription = "昨日在找书的时候无意间发现，“远瞳”的《黎明之剑》突然冲上了起点热搜榜首位，去小说中查找原因，原来是这部六百多万字的作品已经完结了。四年的时间，这部小说始终占据科幻分类前三甲的位置，不得不说“远瞳”的实力的确不容小觑。",
                    rawDescription = "昨日在找书的时候无意间发现，“远瞳”的《黎明之剑》突然冲上了起点热搜榜首位，去小说中查找原因，原来是这部六百多万字的作品已经完结了。四年的时间，这部小说始终占据科幻分类前三甲的位置，不得不说“远瞳”的实力的确不容小觑。",
                    link = "",
                    feedId = "",
                    accountId = 0,
                    date = Date(),
                ),
                feed = Feed(
                    id = "",
                    name = "佛门射手",
                    icon = "",
                    accountId = 0,
                    groupId = "",
                    url = "",
                )
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterBar(modifier = Modifier.padding(horizontal = 12.dp), filter = filter) {
            filter = it
        }
    }
}
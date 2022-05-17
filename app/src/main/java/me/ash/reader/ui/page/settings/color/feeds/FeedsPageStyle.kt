package me.ash.reader.ui.page.settings.color.feeds

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
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
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.model.Filter
import me.ash.reader.data.entity.Group
import me.ash.reader.data.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.page.home.feeds.GroupItem
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onDark
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsPageStyle(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val filterBarStyle = LocalFeedsFilterBarStyle.current
    val filterBarFilled = LocalFeedsFilterBarFilled.current
    val filterBarPadding = LocalFeedsFilterBarPadding.current
    val filterBarTonalElevation = LocalFeedsFilterBarTonalElevation.current
    val topBarTonalElevation = LocalFeedsTopBarTonalElevation.current
    val groupListExpand = LocalFeedsGroupListExpand.current
    val groupListTonalElevation = LocalFeedsGroupListTonalElevation.current

    val scope = rememberCoroutineScope()

    var filterBarStyleDialogVisible by remember { mutableStateOf(false) }
    var filterBarPaddingDialogVisible by remember { mutableStateOf(false) }
    var filterBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var topBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var groupListTonalElevationDialogVisible by remember { mutableStateOf(false) }

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
                    DisplayText(text = stringResource(R.string.feeds_page), desc = "")
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
                        FeedsPagePreview(
                            topBarTonalElevation = topBarTonalElevation,
                            groupListExpand = groupListExpand,
                            groupListTonalElevation = groupListTonalElevation,
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
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${topBarTonalElevation.value}dp",
                        onClick = {
                            topBarTonalElevationDialogVisible = true
                        },
                    ) {}
//                    Tips(text = stringResource(R.string.tips_top_bar_tonal_elevation))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Group List
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.group_list)
                    )
                    SettingItem(
                        title = stringResource(R.string.always_expand),
                        onClick = {
                            (!groupListExpand).put(context, scope)
                        },
                    ) {
                        Switch(activated = groupListExpand.value) {
                            (!groupListExpand).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${groupListTonalElevation.value}dp",
                        onClick = {
                            groupListTonalElevationDialogVisible = true
                        },
                    ) {}
                    Tips(text = stringResource(R.string.tips_group_list_tonal_elevation))
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
                        Switch(activated = filterBarFilled.value) {
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
        options = FeedsFilterBarStylePreference.values.map {
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
            FeedsFilterBarPaddingPreference.put(context, scope, filterBarPaddingValue ?: 0)
            filterBarPaddingDialogVisible = false
        }
    )

    RadioDialog(
        visible = filterBarTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FeedsFilterBarTonalElevationPreference.values.map {
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
        options = FeedsTopBarTonalElevationPreference.values.map {
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
        visible = groupListTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FeedsGroupListTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == groupListTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        groupListTonalElevationDialogVisible = false
    }
}

@Composable
fun FeedsPagePreview(
    topBarTonalElevation: FeedsTopBarTonalElevationPreference,
    groupListExpand: FeedsGroupListExpandPreference,
    groupListTonalElevation: FeedsGroupListTonalElevationPreference,
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
                    groupListTonalElevation.value.dp
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
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {}
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.subscribe),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {}
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        GroupItem(
            isExpanded = groupListExpand.value,
            tonalElevation = groupListTonalElevation.value.dp,
            group = Group(
                id = "",
                name = stringResource(R.string.defaults),
                accountId = 0,
            ),
            feeds = listOf(
                Feed(
                    id = "",
                    name = stringResource(R.string.preview_feed_name),
                    icon = "",
                    accountId = 0,
                    groupId = "",
                    url = "",
                ).apply {
                    important = 100
                }
            ),
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
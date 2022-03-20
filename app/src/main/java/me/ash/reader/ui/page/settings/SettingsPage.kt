package me.ash.reader.ui.page.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.ui.extension.paddingFixedHorizontal
import me.ash.reader.ui.extension.roundClick
import me.ash.reader.ui.widget.TopTitleBox

@Composable
fun SettingsPage(
    navController: NavHostController,
) {
    val listState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()) {
//        LargeTopAppBar(
//            title = { Text(text = "Settings") }
//        )
        TopTitleBox(
            title = "Settings",
            description = "",
            listState = listState,
            startOffset = Offset(20f, 78f),
            startHeight = 72f,
            startTitleFontSize = 36f,
            startDescriptionFontSize = 0f,
        ) {

        }
        Column {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .paddingFixedHorizontal(),
                state = listState
            ) {
                item {
                    Spacer(modifier = Modifier.height(112.dp))
                }
                item {
                    Item(
                        title = "通用",
                        description = "应用的基本设置",
                        imageVector = Icons.Outlined.Apps,
                    )
                }
                item {
                    Item(
                        title = "外观",
                        description = "字体、颜色、背景",
                        imageVector = Icons.Outlined.ColorLens,
                    )
                }
                item {
                    Item(
                        title = "阅读",
                        description = "渲染阅读视图的设置",
                        imageVector = Icons.Outlined.LocalLibrary,
                    )
                }
                item {
                    Item(
                        title = "Ash",
                        description = "本地账户",
                        imageVector = Icons.Outlined.Storage,
                    )
                }
                item {
                    Item(
                        title = "添加账户",
                        description = "FreshRSS、Inoreader、Feedly",
                        imageVector = Icons.Outlined.AccountCircle,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(500.dp))
                    Item(
                        title = "添加账户",
                        description = "FreshRSS、Inoreader、Feedly",
                        imageVector = Icons.Outlined.AccountCircle,
                    )
                }
            }
        }
    }
}

@Composable
fun Item(
    title: String = "",
    description: String = "",
    imageVector: ImageVector,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .roundClick { }
    ) {

        Row(
            modifier = Modifier.paddingFixedHorizontal(top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = imageVector,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsPreview() {
    Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        SettingsPage(navController = NavHostController(LocalContext.current))
    }
}
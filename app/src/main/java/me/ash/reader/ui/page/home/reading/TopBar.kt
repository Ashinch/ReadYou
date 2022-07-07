package me.ash.reader.ui.page.home.reading

import RYExtensibleVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.ext.share
import me.ash.reader.ui.page.common.RouteName

@Composable
fun TopBar(
    navController: NavHostController,
    isShow: Boolean,
    title: String? = "",
    link: String? = "",
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.TopCenter
    ) {
        RYExtensibleVisibility(visible = isShow) {
            SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        onClose()
                    }
                },
                actions = {
                    FeedbackIconButton(
                        modifier = Modifier.size(22.dp),
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = stringResource(R.string.style),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        navController.navigate(RouteName.READING_PAGE_STYLE) {
                            launchSingleTop = true
                        }
                    }
                    FeedbackIconButton(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        context.share(title
                            ?.takeIf { it.isNotBlank() }
                            ?.let { it + "\n" } + link
                        )
                    }
                }
            )
        }
    }
}
package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
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
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.ext.share
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.common.RouteName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    isShow: Boolean,
    windowInsets: WindowInsets = WindowInsets(0.dp),
    title: String? = "",
    link: String? = "",
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current
    val tonalElevation = LocalReadingPageTonalElevation.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.TopCenter
    ) {
        RYExtensibleVisibility(visible = isShow) {
            TopAppBar(
                title = {},
                modifier = Modifier,
                windowInsets = windowInsets,
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
                }, colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation.value.dp),
                )
            )
        }
    }
}

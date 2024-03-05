package me.ash.reader.ui.page.settings.color.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun ReadingImagePage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val readingTheme = LocalReadingTheme.current
    val roundedCorners = LocalReadingImageRoundedCorners.current
    val horizontalPadding = LocalReadingImageHorizontalPadding.current
    val maximize = LocalReadingImageMaximize.current

    var roundedCornersDialogVisible by remember { mutableStateOf(false) }
    var horizontalPaddingDialogVisible by remember { mutableStateOf(false) }

    var roundedCornersValue: Int? by remember { mutableStateOf(roundedCorners) }
    var horizontalPaddingValue: Int? by remember { mutableStateOf(horizontalPadding) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.images), desc = "")
                }

                // Preview
                item {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 24.dp)
//                            .clip(RoundedCornerShape(24.dp))
//                            .background(
//                                MaterialTheme.colorScheme.inverseOnSurface
//                                        onLight MaterialTheme.colorScheme.surface.copy(0.7f)
//                            )
//                            .clickable { },
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        RYAsyncImage(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(24.dp)
//                                .padding(imageHorizontalPadding().dp)
//                                .clip(imageShape()),
//                            data = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1yZWxhdGVkfDJ8fHxlbnwwfHx8fA%3D%3D&auto=format&fit=crop&w=800&q=60",
//                            contentDescription = stringResource(R.string.images),
//                            contentScale = ContentScale.Inside,
//                        )
//                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }


                // Images
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.images)
                    )
                    SettingItem(
                        title = stringResource(R.string.rounded_corners),
                        desc = "${roundedCorners}dp",
                        onClick = { roundedCornersDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.horizontal_padding),
                        desc = "${horizontalPadding}dp",
                        onClick = { horizontalPaddingDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.maximize),
                        onClick = {
                            (!maximize).put(context, scope)
                            ReadingThemePreference.Custom.put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = maximize.value) {
                            (!maximize).put(context, scope)
                            ReadingThemePreference.Custom.put(context, scope)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    TextFieldDialog(
        visible = roundedCornersDialogVisible,
        title = stringResource(R.string.rounded_corners),
        value = (roundedCornersValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            roundedCornersValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            roundedCornersDialogVisible = false
        },
        onConfirm = {
            ReadingImageRoundedCornersPreference.put(context, scope, roundedCornersValue ?: 0)
            ReadingThemePreference.Custom.put(context, scope)
            roundedCornersDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = horizontalPaddingDialogVisible,
        title = stringResource(R.string.horizontal_padding),
        value = (horizontalPaddingValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            horizontalPaddingValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            horizontalPaddingDialogVisible = false
        },
        onConfirm = {
            ReadingImageHorizontalPaddingPreference.put(context, scope, horizontalPaddingValue ?: 0)
            ReadingThemePreference.Custom.put(context, scope)
            horizontalPaddingDialogVisible = false
        }
    )
}

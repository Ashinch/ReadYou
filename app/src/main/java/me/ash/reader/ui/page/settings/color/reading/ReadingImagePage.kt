package me.ash.reader.ui.page.settings.color.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.model.preference.LocalReadingImageHorizontalPadding
import me.ash.reader.data.model.preference.LocalReadingImageRoundedCorners
import me.ash.reader.data.model.preference.ReadingImageHorizontalPaddingPreference
import me.ash.reader.data.model.preference.ReadingImageRoundedCornersPreference
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun ReadingImagePage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val roundedCorners = LocalReadingImageRoundedCorners.current
    val horizontalPadding = LocalReadingImageHorizontalPadding.current

    var roundedCornersDialogVisible by remember { mutableStateOf(false) }
    var horizontalPaddingDialogVisible by remember { mutableStateOf(false) }

    var roundedCornersValue: Int? by remember { mutableStateOf(roundedCorners) }
    var horizontalPaddingValue: Int? by remember { mutableStateOf(horizontalPadding) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.Rounded.ArrowBack,
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
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {

                    }

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

                    }
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
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
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
            horizontalPaddingDialogVisible = false
        }
    )
}

package me.ash.reader.ui.component

import android.os.Build
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.infrastructure.preference.FlowFilterBarStylePreference
import me.ash.reader.infrastructure.preference.LocalThemeIndex
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun FilterBar(
    filter: Filter,
    filterBarStyle: Int,
    filterBarFilled: Boolean,
    filterBarPadding: Dp,
    filterBarTonalElevation: Dp,
    filterOnClick: (Filter) -> Unit = {},
) {
    val view = LocalView.current
    val themeIndex = LocalThemeIndex.current
    val indicatorColor = if (themeIndex == 5 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    } onDark MaterialTheme.colorScheme.secondaryContainer

    NavigationBar(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(filterBarTonalElevation),
        tonalElevation = 0.dp
    ) {
        Spacer(modifier = Modifier.width(filterBarPadding))
        Filter.values.forEach { item ->
            NavigationBarItem(
//                        modifier = Modifier.height(60.dp),
                alwaysShowLabel = when (filterBarStyle) {
                    FlowFilterBarStylePreference.Icon.value -> false
                    FlowFilterBarStylePreference.IconLabel.value -> true
                    FlowFilterBarStylePreference.IconLabelOnlySelected.value -> false
                    else -> false
                },
                icon = {
                    Icon(
                        imageVector = if (filter == item && filterBarFilled) {
                            item.iconFilled
                        } else {
                            item.iconOutline
                        },
                        contentDescription = item.toName()
                    )
                },
                label = if (filterBarStyle == FlowFilterBarStylePreference.Icon.value) {
                    null
                } else {
                    {
                        Text(
                            text = item.toName(),
//                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                selected = filter == item,
                onClick = {
//                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    filterOnClick(item)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = indicatorColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedIconColor = MaterialTheme.colorScheme.contentColorFor(indicatorColor),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        Spacer(modifier = Modifier.width(filterBarPadding))
    }
}

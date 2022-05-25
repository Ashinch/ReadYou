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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import me.ash.reader.data.model.Filter
import me.ash.reader.data.model.getName
import me.ash.reader.data.preference.FlowFilterBarStylePreference
import me.ash.reader.data.preference.LocalThemeIndex
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun FilterBar(
    modifier: Modifier = Modifier,
    filter: Filter,
    filterBarStyle: Int,
    filterBarFilled: Boolean,
    filterBarPadding: Dp,
    filterBarTonalElevation: Dp,
    filterOnClick: (Filter) -> Unit = {},
) {
    val view = LocalView.current
    val themeIndex = LocalThemeIndex.current

    NavigationBar(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(filterBarTonalElevation))
            .navigationBarsPadding(),
        tonalElevation = filterBarTonalElevation,
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
                        contentDescription = item.getName()
                    )
                },
                label = if (filterBarStyle == FlowFilterBarStylePreference.Icon.value) {
                    null
                } else {
                    {
                        Text(
                            text = item.getName(),
                            style = MaterialTheme.typography.labelLarge,
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
                    indicatorColor = if (themeIndex == 5 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    } onDark MaterialTheme.colorScheme.secondaryContainer,
                ),
            )
        }
        Spacer(modifier = Modifier.width(filterBarPadding))
    }
}
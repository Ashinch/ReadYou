package me.ash.reader.ui.page.home

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.data.entity.Filter
import me.ash.reader.data.preference.FlowFilterBarStylePreference
import me.ash.reader.ui.ext.getName
import me.ash.reader.ui.theme.palette.onDark

@OptIn(ExperimentalPagerApi::class)
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

    NavigationBar(
        tonalElevation = filterBarTonalElevation,
    ) {
        Spacer(modifier = Modifier.width(filterBarPadding))
        listOf(
            Filter.Starred,
            Filter.Unread,
            Filter.All,
        ).forEach { item ->
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
                            style = MaterialTheme.typography.labelLarge
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
//                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer alwaysLight true,
//                        unselectedIconColor = MaterialTheme.colorScheme.outline,
//                        selectedTextColor = MaterialTheme.colorScheme.onSurface alwaysLight true,
//                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer onDark MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        }
        Spacer(modifier = Modifier.width(filterBarPadding))
    }
}
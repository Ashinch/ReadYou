package me.ash.reader.ui.page.home

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.data.entity.Filter
import me.ash.reader.data.preference.FilterBarFilledPreference
import me.ash.reader.data.preference.FilterBarFilledPreference.Companion.filterBarFilled
import me.ash.reader.data.preference.FilterBarPaddingPreference
import me.ash.reader.data.preference.FilterBarPaddingPreference.filterBarPadding
import me.ash.reader.data.preference.FilterBarStylePreference
import me.ash.reader.data.preference.FilterBarStylePreference.Companion.filterBarStyle
import me.ash.reader.data.preference.FilterBarTonalElevationPreference
import me.ash.reader.data.preference.FilterBarTonalElevationPreference.Companion.filterBarTonalElevation
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.getName
import me.ash.reader.ui.theme.palette.onDark

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FilterBar(
    modifier: Modifier = Modifier,
    filter: Filter,
    filterOnClick: (Filter) -> Unit = {},
) {
    val view = LocalView.current
    val context = LocalContext.current
    val filterBarStyle =
        context.filterBarStyle.collectAsStateValue(initial = FilterBarStylePreference.default)
    val filterBarFilled =
        context.filterBarFilled.collectAsStateValue(initial = FilterBarFilledPreference.default)
    val filterBarPadding =
        context.filterBarPadding.collectAsStateValue(initial = FilterBarPaddingPreference.default)
    val filterBarTonalElevation =
        context.filterBarTonalElevation.collectAsStateValue(initial = FilterBarTonalElevationPreference.default)

    NavigationBar(
        tonalElevation = filterBarTonalElevation.value.dp,
    ) {
        Spacer(modifier = Modifier.width(filterBarPadding.dp))
        listOf(
            Filter.Starred,
            Filter.Unread,
            Filter.All,
        ).forEach { item ->
            NavigationBarItem(
//                        modifier = Modifier.height(60.dp),
                alwaysShowLabel = when (filterBarStyle) {
                    is FilterBarStylePreference.Icon -> false
                    is FilterBarStylePreference.IconLabel -> true
                    is FilterBarStylePreference.IconLabelOnlySelected -> false
                },
                icon = {
                    Icon(
                        imageVector = if (filter == item && filterBarFilled.value) {
                            item.iconFilled
                        } else {
                            item.iconOutline
                        },
                        contentDescription = item.getName()
                    )
                },
                label = if (filterBarStyle is FilterBarStylePreference.Icon) {
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
        Spacer(modifier = Modifier.width(filterBarPadding.dp))
    }
}
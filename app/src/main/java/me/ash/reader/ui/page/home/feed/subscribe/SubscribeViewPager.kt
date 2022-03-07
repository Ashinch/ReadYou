package me.ash.reader.ui.page.home.feed.subscribe

import androidx.compose.runtime.Composable
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.ui.widget.ViewPager

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SubscribeViewPager(
    inputContent: String = "",
    onValueChange: (String) -> Unit = {},
    onKeyboardAction: () -> Unit = {}
) {
    ViewPager(
        composableList = listOf(
            {
                SearchViewPage(
                    inputContent = inputContent,
                    onValueChange = onValueChange,
                    onKeyboardAction = onKeyboardAction,
                )
            },
            {
                ResultViewPage()
            }
        )
    )
}
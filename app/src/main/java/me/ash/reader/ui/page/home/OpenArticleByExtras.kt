package me.ash.reader.ui.page.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.ash.reader.ui.page.home.read.ReadViewAction
import me.ash.reader.ui.page.home.read.ReadViewModel

@Composable
fun OpenArticleByExtras(
    extrasArticleId: Any? = null,
    homeViewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(extrasArticleId) {
        extrasArticleId?.let {
            readViewModel.dispatch(ReadViewAction.ScrollToItem(2))
            this.launch {
                val article = readViewModel
                    .rssRepository.get()
                    .findArticleById(it.toString()) ?: return@launch
                readViewModel.dispatch(ReadViewAction.InitData(article))
                if (article.feed.isFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                homeViewModel.dispatch(
                    HomeViewAction.ScrollToPage(
                        scope = scope,
                        targetPage = 2,
                    )
                )
            }
        }
    }
}
package me.ash.reader.infrastructure.android

import android.Manifest
import android.content.Intent
import android.database.CursorWindow
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.app.NotificationManagerCompat
import androidx.core.util.Consumer
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.profileinstaller.ProfileInstallerInitializer
import androidx.work.WorkManager
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field
import javax.inject.Inject
import kotlinx.coroutines.launch
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.WidgetUpdateWorker
import me.ash.reader.infrastructure.compose.ProvideCompositionLocals
import me.ash.reader.infrastructure.preference.AccountSettingsProvider
import me.ash.reader.infrastructure.preference.InitialPagePreference
import me.ash.reader.infrastructure.preference.LanguagesPreference
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.initialPage
import me.ash.reader.ui.ext.isFirstLaunch
import me.ash.reader.ui.ext.languages
import me.ash.reader.ui.page.common.ExtraName
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.nav3.AppEntry
import me.ash.reader.ui.page.nav3.key.Route
import me.ash.reader.ui.theme.AppTheme
import timber.log.Timber

/** The Single-Activity Architecture. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var imageLoader: ImageLoader

    @Inject lateinit var settingsProvider: SettingsProvider

    @Inject lateinit var accountService: AccountService

    @Inject lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("RLog", "onCreate: ${ProfileInstallerInitializer().create(this)}")

        enableEdgeToEdge()

        // Set the language
        if (Build.VERSION.SDK_INT < 33) {
            LanguagesPreference.fromValue(languages).let { LanguagesPreference.setLocale(it) }
        }

        // Workaround for https://github.com/ReadYouApp/ReadYou/issues/312: increase cursor window size
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) // 100MB is the new cursor window size
        } catch (e: Exception) {
            Log.e("RLog", "Unable to increase cursor window size: ${e.printStackTrace()}")
        }

        val requestPermissionLauncher =
            this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted
                ->
                if (isGranted) {} else { // Permission denied }
                }
            }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AccountSettingsProvider(accountService = accountService) {
                settingsProvider.ProvidesSettings {
                    val subscribeViewModel: SubscribeViewModel = hiltViewModel()

                    ProvideCompositionLocals {
                        AppTheme(useDarkTheme = LocalDarkTheme.current.isDarkTheme()) {
                            val isFirstLaunch = remember { isFirstLaunch }
                            val initialPage = remember { initialPage }

                            val startDestination = remember {
                                if (isFirstLaunch) listOf(Route.Startup)
                                else if (initialPage == InitialPagePreference.FlowPage.value) {
                                    listOf(Route.Feeds, Route.Reading(null))
                                } else listOf(Route.Feeds)
                            }

                            val backStack =
                                rememberNavBackStack<Route>(*startDestination.toTypedArray())

                            NewIntentHandlerEffect(backStack, subscribeViewModel)
                            AppEntry(backStack)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun NewIntentHandlerEffect(backStack: NavBackStack, subscribeViewModel: SubscribeViewModel) {
        DisposableEffect(backStack) {
            val listener =
                Consumer<Intent> { intent ->
                    intent.getLaunchAction()?.let { action ->
                        when (action) {
                            is LaunchAction.OpenArticle -> {
                                val readingIndex = backStack.indexOfFirst { it is Route.Reading }
                                if (readingIndex != -1) {
                                    backStack.removeRange(readingIndex, backStack.size)
                                }
                                backStack.add(Route.Reading(articleId = action.articleId))
                            }

                            is LaunchAction.Subscribe -> {
                                subscribeViewModel.handleSharedUrlFromIntent(action.url)
                                val feedsIndex = backStack.indexOf(Route.Feeds)
                                if (feedsIndex != -1) {
                                    backStack.removeRange(feedsIndex + 1, backStack.size)
                                } else {
                                    backStack.add(0, Route.Feeds)
                                    backStack.removeRange(1, backStack.size)
                                }
                            }
                        }
                    }
                }
            listener.accept(intent) // consume the launch intent as well
            addOnNewIntentListener(listener)
            onDispose { removeOnNewIntentListener(listener) }
        }
    }

    override fun onResume() {
        WidgetUpdateWorker.enqueueOneTimeWork(workManager)
        super.onResume()
    }
}

sealed interface LaunchAction {
    data class Subscribe(val url: String) : LaunchAction

    data class OpenArticle(val articleId: String) : LaunchAction
}

private fun Intent.getLaunchAction(): LaunchAction? {
    return when (action) {
        Intent.ACTION_VIEW -> {
            dataString?.let { LaunchAction.Subscribe(it) }
        }

        Intent.ACTION_SEND -> {
            getStringExtra(Intent.EXTRA_TEXT)
                ?.also { removeExtra(Intent.EXTRA_TEXT) }
                ?.let { LaunchAction.Subscribe(it) }
        }

        else -> {
            getStringExtra(ExtraName.ARTICLE_ID)
                ?.also { removeExtra(ExtraName.ARTICLE_ID) }
                ?.let { LaunchAction.OpenArticle(it) }
        }
    }
}

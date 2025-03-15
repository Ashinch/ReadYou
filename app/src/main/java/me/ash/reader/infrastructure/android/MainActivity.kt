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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.NotificationManagerCompat
import androidx.core.util.Consumer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.profileinstaller.ProfileInstallerInitializer
import coil.ImageLoader
import coil.compose.LocalImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.infrastructure.preference.AccountSettingsProvider
import me.ash.reader.infrastructure.preference.LanguagesPreference
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.languages
import me.ash.reader.ui.page.common.ExtraName
import me.ash.reader.ui.page.common.HomeEntry
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import java.lang.reflect.Field
import javax.inject.Inject


/**
 * The Single-Activity Architecture.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var accountDao: AccountDao

    lateinit var navController: NavHostController

    private val intentFlow = MutableStateFlow<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("RLog", "onCreate: ${ProfileInstallerInitializer().create(this)}")

        intentFlow.value = intent

        enableEdgeToEdge()

        // Set the language
        if (Build.VERSION.SDK_INT < 33) {
            LanguagesPreference.fromValue(languages).let {
                LanguagesPreference.setLocale(it)
            }
        }

        // Workaround for https://github.com/Ashinch/ReadYou/issues/312: increase cursor window size
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) // 100MB is the new cursor window size
        } catch (e: Exception) {
            Log.e("RLog", "Unable to increase cursor window size: ${e.printStackTrace()}")
        }

        val requestPermissionLauncher = this.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
            } else { // Permission denied }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            navController = rememberNavController()

            LaunchedEffect(Unit) {
                intentFlow.collectLatest { newIntent ->
                    newIntent?.let { handleIntent(it, navController) }
                }
            }

            CompositionLocalProvider(
                LocalImageLoader provides imageLoader,
            ) {
                AccountSettingsProvider(accountDao) {
                    SettingsProvider {
                        val subscribeViewModel: SubscribeViewModel = hiltViewModel()
                        DisposableEffect(this) {
                            val listener = Consumer<Intent> { intent ->
                                intent.getTextOrNull()?.let {
                                    subscribeViewModel.handleSharedUrlFromIntent(it)
                                }
                                handleIntent(intent, navController)
                            }
                            addOnNewIntentListener(listener)
                            onDispose {
                                removeOnNewIntentListener(listener)
                            }
                        }
                        HomeEntry(
                            subscribeViewModel = subscribeViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }

        addOnNewIntentListener (Consumer<Intent> { newIntent ->
            intentFlow.value = newIntent
        })
    }

    private fun handleIntent(intent: Intent, navController: NavHostController) {
        Log.d("MainActivity", "Handing intent $intent")
        Log.d("MainActivity", "Extras: ${intent.extras}")
        val openArticleId = intent.extras?.getString(ExtraName.ARTICLE_ID) ?: ""
        if (openArticleId.isNotEmpty()) {
            // Navigate to specific article
            navController.navigate(RouteName.FLOW) {
                launchSingleTop = true
            }
            navController.navigate("${RouteName.READING}/${openArticleId}") {
                launchSingleTop = true
            }
        } else {
            // Navigate to specific page
            val route = intent.extras?.getString(ExtraName.ROUTE_NAME) ?: ""
            if (route.isNotEmpty()) {
                Log.d("MainActivity", "Got intent, ROUTE_NAME value: $route")
                navController.navigate(route)
            }

        }
    }
}

private fun Intent.getTextOrNull(): String? {

    return when (action) {
        Intent.ACTION_VIEW -> {
            dataString
        }

        Intent.ACTION_SEND -> {
            getStringExtra(Intent.EXTRA_TEXT)
                ?.also { removeExtra(Intent.EXTRA_TEXT) }
        }

        else -> null
    }

}

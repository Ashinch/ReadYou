package me.ash.reader

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.profileinstaller.ProfileInstallerInitializer
import coil.ImageLoader
import coil.compose.LocalImageLoader
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.model.preference.AccountSettingsProvider
import me.ash.reader.data.model.preference.LanguagesPreference
import me.ash.reader.data.model.preference.SettingsProvider
import me.ash.reader.ui.ext.languages
import me.ash.reader.ui.page.common.HomeEntry
import javax.inject.Inject

/**
 * The Single-Activity Architecture.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var accountDao: AccountDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.addFlags(FLAG_LAYOUT_IN_SCREEN or FLAG_LAYOUT_NO_LIMITS)
        }
        Log.i("RLog", "onCreate: ${ProfileInstallerInitializer().create(this)}")

        // Set the language
        LanguagesPreference.fromValue(languages).let {
            if (it == LanguagesPreference.UseDeviceLanguages) return@let
            it.setLocale(this)
        }

        setContent {
            CompositionLocalProvider(
                LocalImageLoader provides imageLoader,
            ) {
                AccountSettingsProvider(accountDao) {
                    SettingsProvider {
                        HomeEntry()
                    }
                }
            }
        }
    }
}

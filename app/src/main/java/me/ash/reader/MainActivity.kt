package me.ash.reader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.profileinstaller.ProfileInstallerInitializer
import coil.ImageLoader
import coil.compose.LocalImageLoader
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.data.preference.LanguagesPreference
import me.ash.reader.data.preference.SettingsProvider
import me.ash.reader.ui.ext.languages
import me.ash.reader.ui.page.common.HomeEntry
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
                SettingsProvider {
                    HomeEntry()
                }
            }
        }
    }
}
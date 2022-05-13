package me.ash.reader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.profileinstaller.ProfileInstallerInitializer
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.data.preference.LanguagesPreference
import me.ash.reader.data.preference.SettingsProvider
import me.ash.reader.ui.ext.languages
import me.ash.reader.ui.page.common.HomeEntry

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
            SettingsProvider {
                HomeEntry()
            }
        }
    }
}
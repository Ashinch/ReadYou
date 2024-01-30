package me.ash.reader.infrastructure.android

import android.database.CursorWindow
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.profileinstaller.ProfileInstallerInitializer
import coil.ImageLoader
import coil.compose.LocalImageLoader
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.infrastructure.preference.AccountSettingsProvider
import me.ash.reader.infrastructure.preference.LanguagesPreference
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.languages
import me.ash.reader.ui.page.common.HomeEntry
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.addFlags(FLAG_LAYOUT_IN_SCREEN or FLAG_LAYOUT_NO_LIMITS)
        }
        Log.i("RLog", "onCreate: ${ProfileInstallerInitializer().create(this)}")

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

package me.ash.reader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.profileinstaller.ProfileInstallerInitializer
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.ui.page.common.HomeEntry

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Log.i("RLog", "onCreate: ${ProfileInstallerInitializer().create(this)}")
        setContent {
            HomeEntry(intent.extras).also {
                intent.replaceExtras(null)
            }
        }
    }
}
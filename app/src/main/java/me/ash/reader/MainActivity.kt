package me.ash.reader

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.profileinstaller.ProfileInstallerInitializer
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import me.ash.reader.data.preference.SettingsProvider
import me.ash.reader.ui.page.common.HomeEntry

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ImageLoader {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Log.i("RLog", "onCreate: ${ProfileInstallerInitializer().create(this)}")
        setContent {
            SettingsProvider {
                HomeEntry()
            }
        }
    }

    override val components: ComponentRegistry
        get() = ComponentRegistry.Builder().add(SvgDecoder.Factory()).build()
    override val defaults: DefaultRequestOptions
        get() = DefaultRequestOptions()
    override val diskCache: DiskCache
        get() = DiskCache.Builder()
            .directory(this.cacheDir.resolve("images"))
            .maxSizePercent(0.02)
            .build()
    override val memoryCache: MemoryCache
        get() = MemoryCache.Builder(this)
            .maxSizePercent(0.25)
            .build()

    override fun enqueue(request: ImageRequest): Disposable {
        // Always call onStart before onSuccess.
        request.target?.onStart(request.placeholder)
        val result = ColorDrawable(Color.BLACK)
        request.target?.onSuccess(result)
        return object : Disposable {
            override val job = CompletableDeferred(newResult(request, result))
            override val isDisposed get() = true
            override fun dispose() {}
        }
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        return newResult(request, ColorDrawable(Color.BLACK))
    }

    override fun newBuilder(): ImageLoader.Builder {
        throw UnsupportedOperationException()
    }

    override fun shutdown() {
    }

    private fun newResult(request: ImageRequest, drawable: Drawable): SuccessResult {
        return SuccessResult(
            drawable = drawable,
            request = request,
            dataSource = DataSource.MEMORY_CACHE
        )
    }
}
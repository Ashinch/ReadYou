package me.ash.reader

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.*
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.module.DispatcherDefault
import me.ash.reader.data.repository.*
import me.ash.reader.data.source.AppNetworkDataSource
import me.ash.reader.data.source.OpmlLocalDataSource
import me.ash.reader.data.source.ReaderDatabase
import me.ash.reader.ui.ext.*
import org.conscrypt.Conscrypt
import java.security.Security
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, ImageLoader {
    init {
        // From: https://gitlab.com/spacecowboy/Feeder
        // Install Conscrypt to handle TLSv1.3 pre Android10
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }

    @Inject
    lateinit var readerDatabase: ReaderDatabase

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var appNetworkDataSource: AppNetworkDataSource

    @Inject
    lateinit var opmlLocalDataSource: OpmlLocalDataSource

    @Inject
    lateinit var rssHelper: RssHelper

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var stringsRepository: StringsRepository

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var localRssRepository: LocalRssRepository

//    @Inject
//    lateinit var feverRssRepository: FeverRssRepository

    @Inject
    lateinit var opmlRepository: OpmlRepository

    @Inject
    lateinit var rssRepository: RssRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    @DispatcherDefault
    lateinit var dispatcherDefault: CoroutineDispatcher

    override fun onCreate() {
        super.onCreate()
        CrashHandler(this)
        dataStoreInit()
        applicationScope.launch(dispatcherDefault) {
            accountInit()
            workerInit()
            if (BuildConfig.FLAVOR != "fdroid") {
                checkUpdate()
            }
        }
    }

    private fun dataStoreInit() {
    }

    private suspend fun accountInit() {
        if (accountRepository.isNoAccount()) {
            val account = accountRepository.addDefaultAccount()
            applicationContext.dataStore.put(DataStoreKeys.CurrentAccountId, account.id!!)
            applicationContext.dataStore.put(DataStoreKeys.CurrentAccountType, account.type)
        }
    }

    private fun workerInit() {
        rssRepository.get().doSync()
    }

    private suspend fun checkUpdate() {
        applicationContext.getLatestApk().let {
            if (it.exists()) {
                it.del()
            }
        }
        appRepository.checkUpdate(showToast = false)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override val components: ComponentRegistry
        get() = ComponentRegistry.Builder()
            .add(SvgDecoder.Factory())
            .add(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                }
            )
            .build()
    override val defaults: DefaultRequestOptions
        get() = DefaultRequestOptions()
    override val diskCache: DiskCache
        get() = DiskCache.Builder()
            .directory(cacheDir.resolve("images"))
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
package me.ash.reader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ash.reader.data.repository.AccountRepository
import me.ash.reader.data.repository.ArticleRepository
import me.ash.reader.data.repository.OpmlRepository
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.source.OpmlLocalDataSource
import me.ash.reader.data.source.ReaderDatabase
import me.ash.reader.data.source.RssNetworkDataSource
import javax.inject.Inject

@DelicateCoroutinesApi
@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var readerDatabase: ReaderDatabase

    @Inject
    lateinit var opmlLocalDataSource: OpmlLocalDataSource

    @Inject
    lateinit var rssNetworkDataSource: RssNetworkDataSource

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Inject
    lateinit var opmlRepository: OpmlRepository

    @Inject
    lateinit var rssRepository: RssRepository

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            if (accountRepository.isNoAccount()) {
                val accountId = accountRepository.addDefaultAccount()
                applicationContext.dataStore.put(DataStoreKeys.CurrentAccountId, accountId)
            }
            rssRepository.sync(true)
        }
    }
}
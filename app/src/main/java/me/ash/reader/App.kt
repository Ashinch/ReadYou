package me.ash.reader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ash.reader.data.repository.*
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
    lateinit var rssHelper: RssHelper

    @Inject
    lateinit var stringsRepository: StringsRepository

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var localRssRepository: LocalRssRepository

    @Inject
    lateinit var feverRssRepository: FeverRssRepository

    @Inject
    lateinit var opmlRepository: OpmlRepository

    @Inject
    lateinit var rssRepository: RssRepository

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            if (accountRepository.isNoAccount()) {
                val account = accountRepository.addDefaultAccount()
                applicationContext.dataStore.put(DataStoreKeys.CurrentAccountId, account.id!!)
                applicationContext.dataStore.put(DataStoreKeys.CurrentAccountType, account.type)
            }
            rssRepository.get().doSync(true)
        }
    }
}
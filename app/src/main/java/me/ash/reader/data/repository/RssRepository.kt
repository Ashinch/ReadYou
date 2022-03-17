package me.ash.reader.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.DataStoreKeys
import me.ash.reader.data.account.Account
import me.ash.reader.dataStore
import me.ash.reader.get
import javax.inject.Inject

class RssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val localRssRepository: LocalRssRepository,
//    private val feverRssRepository: FeverRssRepository,
//    private val googleReaderRssRepository: GoogleReaderRssRepository,
) {
    fun get() = when (getAccountType()) {
//        Account.Type.LOCAL -> localRssRepository
        Account.Type.LOCAL -> localRssRepository
//        Account.Type.FEVER -> feverRssRepository
//        Account.Type.GOOGLE_READER -> googleReaderRssRepository
        else -> throw IllegalStateException("Unknown account type: ${getAccountType()}")
    }

    private fun getAccountType(): Int = context.dataStore.get(DataStoreKeys.CurrentAccountType)!!
}

package me.ash.reader.domain.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.ui.ext.currentAccountType
import javax.inject.Inject

class RssService @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val localRssService: LocalRssService,
    private val feverRssService: FeverRssService,
    private val googleReaderRssService: GoogleReaderRssService,
) {

    fun get() = get(context.currentAccountType)

    fun get(accountTypeId: Int) = when (accountTypeId) {
        AccountType.Local.id -> localRssService
        AccountType.Fever.id -> feverRssService
        AccountType.GoogleReader.id -> googleReaderRssService
        AccountType.FreshRSS.id -> googleReaderRssService
        AccountType.Inoreader.id -> localRssService
        AccountType.Feedly.id -> localRssService
        else -> localRssService
    }
}

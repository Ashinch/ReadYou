package me.ash.reader.domain.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.ui.ext.currentAccountType
import javax.inject.Inject

class RssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val localRssRepository: LocalRssRepository,
    private val feverRssRepository: FeverRssRepository,
//    private val googleReaderRssRepository: GoogleReaderRssRepository,
) {

    fun get() = get(context.currentAccountType)

    fun get(accountTypeId: Int) = when (accountTypeId) {
        AccountType.Local.id -> localRssRepository
        AccountType.Fever.id -> feverRssRepository
        AccountType.GoogleReader.id -> localRssRepository
        AccountType.Inoreader.id -> localRssRepository
        AccountType.Feedly.id -> localRssRepository
        else -> localRssRepository
    }
}

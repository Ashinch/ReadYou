package me.ash.reader.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.data.entity.Account
import me.ash.reader.ui.ext.currentAccountType
import javax.inject.Inject

class RssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val localRssRepository: LocalRssRepository,
//    private val feverRssRepository: FeverRssRepository,
//    private val googleReaderRssRepository: GoogleReaderRssRepository,
) {
    fun get() = when (context.currentAccountType) {
        Account.Type.LOCAL -> localRssRepository
//        Account.Type.LOCAL -> feverRssRepository
//        Account.Type.FEVER -> feverRssRepository
//        Account.Type.GOOGLE_READER -> googleReaderRssRepository
        else -> throw IllegalStateException("Unknown account type: ${context.currentAccountType}")
    }
}

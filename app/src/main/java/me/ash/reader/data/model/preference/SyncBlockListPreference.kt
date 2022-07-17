package me.ash.reader.data.model.preference

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.ui.ext.currentAccountId

typealias SyncBlockList = List<String>

object SyncBlockListPreference {

    val default: SyncBlockList = emptyList()

    fun put(accountDao: AccountDao, context: Context, scope: CoroutineScope) {
        scope.launch {
            accountDao.queryById(context.currentAccountId)?.let {
                accountDao.update(it.apply { syncBlockList = this@SyncBlockListPreference.default })
            }
        }
    }
}

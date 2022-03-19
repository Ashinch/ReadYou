package me.ash.reader.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.DataStoreKeys
import me.ash.reader.data.account.Account
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupDao
import me.ash.reader.dataStore
import me.ash.reader.get
import me.ash.reader.spacerDollar
import javax.inject.Inject

class AccountRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
) {

    suspend fun getCurrentAccount(): Account? {
        val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        return accountDao.queryById(accountId)
    }

    suspend fun isNoAccount(): Boolean {
        return accountDao.queryAll().isEmpty()
    }

    suspend fun addDefaultAccount(): Account {
        return Account(
            name = "Read You",
            type = Account.Type.LOCAL,
        ).apply {
            id = accountDao.insert(this).toInt()
        }.also {
            if (groupDao.queryAll(it.id!!).isEmpty()) {
                groupDao.insert(
                    Group(
                        id = it.id!!.spacerDollar("0"),
                        name = "默认",
                        accountId = it.id!!,
                    )
                )
            }
        }
    }
}
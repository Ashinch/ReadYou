package me.ash.reader.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.R
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.entity.Account
import me.ash.reader.data.entity.Group
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import javax.inject.Inject

class AccountRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
) {

    suspend fun getCurrentAccount(): Account? {
        return accountDao.queryById(context.currentAccountId)
    }

    suspend fun isNoAccount(): Boolean {
        return accountDao.queryAll().isEmpty()
    }

    suspend fun addDefaultAccount(): Account {
        val readYouString = context.getString(R.string.read_you)
        val defaultString = context.getString(R.string.defaults)
        return Account(
            name = readYouString,
            type = Account.Type.LOCAL,
        ).apply {
            id = accountDao.insert(this).toInt()
        }.also {
            if (groupDao.queryAll(it.id!!).isEmpty()) {
                groupDao.insert(
                    Group(
                        id = it.id!!.getDefaultGroupId(),
                        name = defaultString,
                        accountId = it.id!!,
                    )
                )
            }
        }
    }
}
package me.ash.reader.domain.service

import android.content.Context
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.getDefaultGroupId
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.showToast
import javax.inject.Inject

class AccountService @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val rssService: RssService,
) {

    fun getAccounts(): Flow<List<Account>> = accountDao.queryAllAsFlow()

    fun getAccountById(accountId: Int): Flow<Account?> = accountDao.queryAccount(accountId)

    suspend fun getCurrentAccount(): Account = accountDao.queryById(context.currentAccountId)!!

    suspend fun isNoAccount(): Boolean = accountDao.queryAll().isEmpty()

    suspend fun addAccount(account: Account): Account =
        account.apply {
            id = accountDao.insert(this).toInt()
        }.also {
            // handle default group
            when (it.type) {
                AccountType.Local -> {
                    groupDao.insert(
                        Group(
                            id = it.id!!.getDefaultGroupId(),
                            name = context.getString(R.string.defaults),
                            accountId = it.id!!,
                        )
                    )
                }
            }
            context.dataStore.put(DataStoreKey.currentAccountId, it.id!!)
            context.dataStore.put(DataStoreKey.currentAccountType, it.type.id)
        }

    suspend fun addDefaultAccount(): Account =
        addAccount(Account(
            type = AccountType.Local,
            name = context.getString(R.string.read_you),
        ))

    suspend fun update(accountId: Int, block: Account.() -> Unit) {
        accountDao.queryById(accountId)?.let {
            accountDao.update(it.apply(block))
        }
    }

    suspend fun delete(accountId: Int) {
        if (accountDao.queryAll().size == 1) {
            Looper.myLooper() ?: Looper.prepare()
            context.showToast(context.getString(R.string.must_have_an_account))
            Looper.loop()
            return
        }
        rssService.get().cancelSync()
        accountDao.queryById(accountId)?.let {
            articleDao.deleteByAccountId(accountId)
            feedDao.deleteByAccountId(accountId)
            groupDao.deleteByAccountId(accountId)
            accountDao.delete(it)
            accountDao.queryAll().getOrNull(0)?.let {
                context.dataStore.put(DataStoreKey.currentAccountId, it.id!!)
                context.dataStore.put(DataStoreKey.currentAccountType, it.type.id)
            }
        }
    }

    suspend fun switch(account: Account) {
        rssService.get().cancelSync()
        context.dataStore.put(DataStoreKey.currentAccountId, account.id!!)
        context.dataStore.put(DataStoreKey.currentAccountType, account.type.id)
    }
}

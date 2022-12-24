package me.ash.reader.ui.page.settings.accounts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.data.model.account.Account
import me.ash.reader.data.module.DefaultDispatcher
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.data.module.MainDispatcher
import me.ash.reader.data.repository.AccountRepository
import me.ash.reader.data.repository.OpmlRepository
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val rssRepository: RssRepository,
    private val opmlRepository: OpmlRepository,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _accountUiState = MutableStateFlow(AccountUiState())
    val accountUiState: StateFlow<AccountUiState> = _accountUiState.asStateFlow()
    val accounts = accountRepository.getAccounts()

    fun initData(accountId: Int) {
        viewModelScope.launch(ioDispatcher) {
            _accountUiState.update { it.copy(selectedAccount = accountRepository.getAccountById(accountId)) }
        }
    }

    fun update(accountId: Int, block: Account.() -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            accountRepository.update(accountId, block)
        }
    }

    fun exportAsOPML(accountId: Int, callback: (String) -> Unit = {}) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                callback(opmlRepository.saveToString(accountId))
            } catch (e: Exception) {
                Log.e("FeedsViewModel", "exportAsOpml: ", e)
            }
        }
    }

    fun hideDeleteDialog() {
        _accountUiState.update { it.copy(deleteDialogVisible = false) }
    }

    fun showDeleteDialog() {
        _accountUiState.update { it.copy(deleteDialogVisible = true) }
    }

    fun showClearDialog() {
        _accountUiState.update { it.copy(clearDialogVisible = true) }
    }

    fun hideClearDialog() {
        _accountUiState.update { it.copy(clearDialogVisible = false) }
    }

    fun delete(accountId: Int, callback: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            accountRepository.delete(accountId)
            withContext(mainDispatcher) {
                callback()
            }
        }
    }

    fun clear(account: Account, callback: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            rssRepository.get(account.type.id).deleteAccountArticles(account.id!!)
            withContext(mainDispatcher) {
                callback()
            }
        }
    }

    fun addAccount(account: Account, callback: (Account?) -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            val addAccount = accountRepository.addAccount(account)
            try {
                if (rssRepository.get(addAccount.type.id).validCredentials()) {
                    withContext(mainDispatcher) {
                        callback(addAccount)
                    }
                } else {
                    throw Exception("Unauthorized")
                }
            } catch (e: Exception) {
                accountRepository.delete(account.id!!)
                withContext(mainDispatcher) {
                    callback(null)
                }
            }
        }
    }

    fun switchAccount(targetAccount: Account, callback: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            accountRepository.switch(targetAccount)
            withContext(mainDispatcher) {
                callback()
            }
        }
    }
}

data class AccountUiState(
    val selectedAccount: Flow<Account?> = emptyFlow(),
    val deleteDialogVisible: Boolean = false,
    val clearDialogVisible: Boolean = false,
)

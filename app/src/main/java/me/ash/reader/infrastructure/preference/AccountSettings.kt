package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.currentAccountId

// Accounts
val LocalSyncInterval = compositionLocalOf<SyncIntervalPreference> { SyncIntervalPreference.default }
val LocalSyncOnStart = compositionLocalOf<SyncOnStartPreference> { SyncOnStartPreference.default }
val LocalSyncOnlyOnWiFi = compositionLocalOf<SyncOnlyOnWiFiPreference> { SyncOnlyOnWiFiPreference.default }
val LocalSyncOnlyWhenCharging =
    compositionLocalOf<SyncOnlyWhenChargingPreference> { SyncOnlyWhenChargingPreference.default }
val LocalKeepArchived = compositionLocalOf<KeepArchivedPreference> { KeepArchivedPreference.default }
val LocalSyncBlockList = compositionLocalOf { SyncBlockListPreference.default }

@Composable
fun AccountSettingsProvider(
    accountDao: AccountDao,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val accountSettings = accountDao.queryAccount(context.currentAccountId).collectAsStateValue(initial = null)

    CompositionLocalProvider(
        // Accounts
        LocalSyncInterval provides (accountSettings?.syncInterval ?: SyncIntervalPreference.default),
        LocalSyncOnStart provides (accountSettings?.syncOnStart ?: SyncOnStartPreference.default),
        LocalSyncOnlyOnWiFi provides (accountSettings?.syncOnlyOnWiFi ?: SyncOnlyOnWiFiPreference.default),
        LocalSyncOnlyWhenCharging provides (accountSettings?.syncOnlyWhenCharging
            ?: SyncOnlyWhenChargingPreference.default),
        LocalKeepArchived provides (accountSettings?.keepArchived ?: KeepArchivedPreference.default),
        LocalSyncBlockList provides (accountSettings?.syncBlockList ?: SyncBlockListPreference.default),
    ) {
        content()
    }
}


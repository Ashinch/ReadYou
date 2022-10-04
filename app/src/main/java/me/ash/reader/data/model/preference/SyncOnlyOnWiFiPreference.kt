package me.ash.reader.data.model.preference

import android.content.Context
import me.ash.reader.R
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

sealed class SyncOnlyOnWiFiPreference(
    val value: Boolean,
) {

    object On : SyncOnlyOnWiFiPreference(true)
    object Off : SyncOnlyOnWiFiPreference(false)

    fun put(viewModel: AccountViewModel) {
        viewModel.update { syncOnlyOnWiFi = this@SyncOnlyOnWiFiPreference }
    }

    fun toDesc(context: Context): String =
        when (this) {
            On -> context.getString(R.string.on)
            Off -> context.getString(R.string.off)
        }

    companion object {

        val default = Off
        val values = listOf(On, Off)
    }
}

operator fun SyncOnlyOnWiFiPreference.not(): SyncOnlyOnWiFiPreference =
    when (value) {
        true -> SyncOnlyOnWiFiPreference.Off
        false -> SyncOnlyOnWiFiPreference.On
    }

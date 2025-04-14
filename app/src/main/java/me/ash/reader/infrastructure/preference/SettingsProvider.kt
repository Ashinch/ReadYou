package me.ash.reader.infrastructure.preference

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.ui.ext.dataStore
import javax.inject.Inject

class SettingsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope coroutineScope: CoroutineScope,
    @IODispatcher ioDispatcher: CoroutineDispatcher
) {
    private val _settingsFlow = MutableStateFlow(Settings())
    val settingsFlow: StateFlow<Settings> = _settingsFlow
    val settings: Settings get() = settingsFlow.value

    init {
        coroutineScope.launch(ioDispatcher) {
            context.dataStore.data.collect {
                _settingsFlow.value = it.toSettings()
            }
        }
    }
}
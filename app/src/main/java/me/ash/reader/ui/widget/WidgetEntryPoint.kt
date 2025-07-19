package me.ash.reader.ui.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface WidgetEntryPoint {
    fun repository(): WidgetRepository
}

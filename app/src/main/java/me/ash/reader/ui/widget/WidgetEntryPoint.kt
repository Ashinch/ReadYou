package me.ash.reader.ui.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.service.AccountService

@InstallIn(SingletonComponent::class)
@EntryPoint
interface WidgetEntryPoint {
    fun accountService(): AccountService
    fun articleDao(): ArticleDao
}
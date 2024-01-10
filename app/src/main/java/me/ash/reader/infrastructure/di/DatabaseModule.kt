package me.ash.reader.infrastructure.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.source.RYDatabase
import javax.inject.Singleton

/**
 * Provides Data Access Objects for database.
 *
 * - [ArticleDao]
 * - [FeedDao]
 * - [GroupDao]
 * - [AccountDao]
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArticleDao(ryDatabase: RYDatabase): ArticleDao =
        ryDatabase.articleDao()

    @Provides
    @Singleton
    fun provideFeedDao(ryDatabase: RYDatabase): FeedDao =
        ryDatabase.feedDao()

    @Provides
    @Singleton
    fun provideGroupDao(ryDatabase: RYDatabase): GroupDao =
        ryDatabase.groupDao()

    @Provides
    @Singleton
    fun provideAccountDao(ryDatabase: RYDatabase): AccountDao =
        ryDatabase.accountDao()

    @Provides
    @Singleton
    fun provideReaderDatabase(@ApplicationContext context: Context): RYDatabase =
        RYDatabase.getInstance(context)
}

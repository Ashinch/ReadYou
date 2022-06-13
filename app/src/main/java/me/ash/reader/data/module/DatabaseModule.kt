package me.ash.reader.data.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.source.RYDatabase
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

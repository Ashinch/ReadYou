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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArticleDao(RYDatabase: RYDatabase): ArticleDao =
        RYDatabase.articleDao()

    @Provides
    @Singleton
    fun provideFeedDao(RYDatabase: RYDatabase): FeedDao =
        RYDatabase.feedDao()

    @Provides
    @Singleton
    fun provideGroupDao(RYDatabase: RYDatabase): GroupDao =
        RYDatabase.groupDao()

    @Provides
    @Singleton
    fun provideAccountDao(RYDatabase: RYDatabase): AccountDao =
        RYDatabase.accountDao()

    @Provides
    @Singleton
    fun provideReaderDatabase(@ApplicationContext context: Context): RYDatabase =
        RYDatabase.getInstance(context)
}
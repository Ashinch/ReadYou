package me.ash.reader.data.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.source.ReaderDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArticleDao(readerDatabase: ReaderDatabase): ArticleDao =
        readerDatabase.articleDao()

    @Provides
    @Singleton
    fun provideFeedDao(readerDatabase: ReaderDatabase): FeedDao =
        readerDatabase.feedDao()

    @Provides
    @Singleton
    fun provideGroupDao(readerDatabase: ReaderDatabase): GroupDao =
        readerDatabase.groupDao()

    @Provides
    @Singleton
    fun provideAccountDao(readerDatabase: ReaderDatabase): AccountDao =
        readerDatabase.accountDao()

    @Provides
    @Singleton
    fun provideReaderDatabase(@ApplicationContext context: Context): ReaderDatabase =
        ReaderDatabase.getInstance(context)
}
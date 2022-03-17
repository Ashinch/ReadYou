package me.ash.reader.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.ash.reader.data.source.FeverApiDataSource
import me.ash.reader.data.source.GoogleReaderApiDataSource
import me.ash.reader.data.source.RssNetworkDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {
    @Singleton
    @Provides
    fun provideRssNetworkDataSource(): RssNetworkDataSource =
        RssNetworkDataSource.getInstance()

    @Singleton
    @Provides
    fun provideFeverApiDataSource(): FeverApiDataSource =
        FeverApiDataSource.getInstance()

    @Singleton
    @Provides
    fun provideGoogleReaderApiDataSource(): GoogleReaderApiDataSource =
        GoogleReaderApiDataSource.getInstance()
}
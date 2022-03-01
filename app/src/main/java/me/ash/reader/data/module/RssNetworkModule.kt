package me.ash.reader.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.ash.reader.data.source.RssNetworkDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RssNetworkModule {
    @Singleton
    @Provides
    fun provideRssNetworkDataSource(): RssNetworkDataSource =
        RssNetworkDataSource.getInstance()
}
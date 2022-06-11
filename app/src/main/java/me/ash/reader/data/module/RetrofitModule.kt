package me.ash.reader.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.ash.reader.data.source.FeverApiDataSource
import me.ash.reader.data.source.GoogleReaderApiDataSource
import me.ash.reader.data.source.RYNetworkDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideAppNetworkDataSource(): RYNetworkDataSource =
        RYNetworkDataSource.getInstance()

    @Provides
    @Singleton
    fun provideFeverApiDataSource(): FeverApiDataSource =
        FeverApiDataSource.getInstance()

    @Provides
    @Singleton
    fun provideGoogleReaderApiDataSource(): GoogleReaderApiDataSource =
        GoogleReaderApiDataSource.getInstance()
}

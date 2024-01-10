package me.ash.reader.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.ash.reader.infrastructure.source.RYNetworkDataSource
import javax.inject.Singleton

/**
 * Provides network requests for Retrofit.
 *
 * - [RYNetworkDataSource]: For network requests within the application
 */
@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideAppNetworkDataSource(): RYNetworkDataSource =
        RYNetworkDataSource.getInstance()
}

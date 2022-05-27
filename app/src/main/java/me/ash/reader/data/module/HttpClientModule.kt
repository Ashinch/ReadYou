package me.ash.reader.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {
    @Provides
    @Singleton
    fun httpClient(httpClientDispatcher: Dispatcher): OkHttpClient =
        OkHttpClient.Builder()
            .dispatcher(httpClientDispatcher)
            .build()

    @Provides
    @Singleton
    fun httpClientDispatcher(): Dispatcher = Dispatcher()
}

package me.ash.reader.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rust.nostr.sdk.Client
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NostrClientModule {

    @Provides
    @Singleton
    fun provideNostrClient(): Client {
        return Client()
    }
}
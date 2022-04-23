package me.ash.reader.data.source

import me.ash.reader.data.entity.GitHubRelease
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface AppNetworkDataSource {
    @GET("https://api.github.com/repos/Ashinch/ReadYou/releases/latest")
    suspend fun getReleaseLatest(): GitHubRelease

    companion object {
        private var instance: AppNetworkDataSource? = null

        fun getInstance(): AppNetworkDataSource {
            return instance ?: synchronized(this) {
                instance ?: Retrofit.Builder()
                    .baseUrl("https://api.github.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(AppNetworkDataSource::class.java).also {
                        instance = it
                    }
            }
        }
    }
}
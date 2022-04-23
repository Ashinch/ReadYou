package me.ash.reader.data.source

import me.ash.reader.data.entity.LatestRelease
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface AppNetworkDataSource {
    @GET
    suspend fun getReleaseLatest(@Url url: String): LatestRelease

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
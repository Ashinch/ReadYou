package me.ash.reader.data.source

import com.github.muhrifqii.parserss.RSSFeedObject
import com.github.muhrifqii.parserss.retrofit.ParseRSSConverterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface RssNetworkDataSource {
    @GET
    suspend fun parseRss(@Url url: String): RSSFeedObject

    companion object {
        private var instance: RssNetworkDataSource? = null

        fun getInstance(): RssNetworkDataSource {
            return instance ?: synchronized(this) {
                instance ?: Retrofit.Builder()
                    .baseUrl("https://api.feeddd.org/feeds/")
                    .addConverterFactory(ParseRSSConverterFactory.create<RSSFeedObject>())
                    .build().create(RssNetworkDataSource::class.java).also {
                        instance = it
                    }
            }
        }
    }
}
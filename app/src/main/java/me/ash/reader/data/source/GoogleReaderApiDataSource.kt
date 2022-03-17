package me.ash.reader.data.source

import com.github.muhrifqii.parserss.ParseRSS
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers
import retrofit2.http.POST

interface GoogleReaderApiDataSource {
    @POST("accounts/ClientLogin")
    fun login(Email: String, Passwd: String): Call<String>

    @Headers("Authorization:GoogleLogin auth=${"Ashinch/678592edaf9145e5b27068d9dc3afc41494ba54e"}")
    @POST("reader/api/0/subscription/list?output=json")
    fun subscriptionList(): Call<GoogleReaderApiDto.SubscriptionList>

    @Headers("Authorization:GoogleLogin auth=${"Ashinch/678592edaf9145e5b27068d9dc3afc41494ba54e"}")
    @POST("reader/api/0/unread-count?output=json")
    fun unreadCount(): Call<GoogleReaderApiDto.UnreadCount>

    @Headers("Authorization:GoogleLogin auth=${"Ashinch/678592edaf9145e5b27068d9dc3afc41494ba54e"}")
    @POST("reader/api/0/tag/list?output=json")
    fun tagList(): Call<GoogleReaderApiDto.TagList>

    @Headers("Authorization:GoogleLogin auth=${"Ashinch/678592edaf9145e5b27068d9dc3afc41494ba54e"}")
    @POST("reader/api/0/stream/contents/reading-list")
    fun readingList(): Call<GoogleReaderApiDto.ReadingList>

    companion object {
        private var instance: GoogleReaderApiDataSource? = null

        fun getInstance(): GoogleReaderApiDataSource {
            return instance ?: synchronized(this) {
                ParseRSS.init(XmlPullParserFactory.newInstance())
                instance ?: Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/api/greader.php/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(GoogleReaderApiDataSource::class.java).also {
                        instance = it
                    }
            }
        }
    }
}
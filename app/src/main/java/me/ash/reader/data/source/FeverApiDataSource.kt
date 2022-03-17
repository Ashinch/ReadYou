package me.ash.reader.data.source

import com.github.muhrifqii.parserss.ParseRSS
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FeverApiDataSource {
    @Multipart
    @POST("fever.php?api&groups")
    fun groups(@Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()): Call<FeverApiDto.Groups>

    @Multipart
    @POST("fever.php?api&feeds")
    fun feeds(@Part("api_key") apiKey: RequestBody?="1352b707f828a6f502db3768fa8d7151".toRequestBody()): Call<FeverApiDto.Feed>

    companion object {
        private var instance: FeverApiDataSource? = null

        fun getInstance(): FeverApiDataSource {
            return instance ?: synchronized(this) {
                ParseRSS.init(XmlPullParserFactory.newInstance())
                instance ?: Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(FeverApiDataSource::class.java).also {
                        instance = it
                    }
            }
        }
    }
}
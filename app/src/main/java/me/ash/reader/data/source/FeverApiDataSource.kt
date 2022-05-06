package me.ash.reader.data.source

import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FeverApiDataSource {
    @Multipart
    @POST("fever.php/?api=&feeds=")
    fun feeds(@Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()): Call<FeverApiDto.Feed>

    @Multipart
    @POST("fever.php/?api=&groups=")
    fun groups(@Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()): Call<FeverApiDto.Groups>

    @Multipart
    @POST("fever.php/?api=&items=")
    fun itemsBySince(
        @Query("since_id") since: Long,
        @Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()
    ): Call<FeverApiDto.Items>

    @Multipart
    @POST("fever.php/?api=&unread_item_ids=")
    fun itemsByUnread(@Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()): Call<FeverApiDto.ItemsByUnread>

    @Multipart
    @POST("fever.php/?api=&saved_item_ids=")
    fun itemsByStarred(@Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()): Call<FeverApiDto.ItemsByStarred>

    @Multipart
    @POST("fever.php/?api=&items=")
    fun itemsByIds(
        @Query("with_ids") ids: String,
        @Part("api_key") apiKey: RequestBody? = "1352b707f828a6f502db3768fa8d7151".toRequestBody()
    ): Call<FeverApiDto.Items>

    companion object {
        private var instance: FeverApiDataSource? = null

        fun getInstance(): FeverApiDataSource {
            return instance ?: synchronized(this) {
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
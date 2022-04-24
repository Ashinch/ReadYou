package me.ash.reader.data.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.ash.reader.data.entity.LatestRelease
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File

sealed class Download {
    object NotYet : Download()
    data class Progress(val percent: Int) : Download()
    data class Finished(val file: File) : Download()
}

interface AppNetworkDataSource {
    @GET
    suspend fun getReleaseLatest(@Url url: String): Response<LatestRelease>

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody

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

fun ResponseBody.downloadToFileWithProgress(saveFile: File): Flow<Download> =
    flow {
        emit(Download.Progress(0))

        // flag to delete file if download errors or is cancelled
        var deleteFile = true

        try {
            byteStream().use { inputStream ->
                saveFile.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val data = ByteArray(8_192)
                    var progressBytes = 0L

                    while (true) {
                        val bytes = inputStream.read(data)

                        if (bytes == -1) {
                            break
                        }

                        outputStream.channel
                        outputStream.write(data, 0, bytes)
                        progressBytes += bytes

                        emit(Download.Progress(percent = ((progressBytes * 100) / totalBytes).toInt()))
                    }

                    when {
                        progressBytes < totalBytes ->
                            throw Exception("missing bytes")
                        progressBytes > totalBytes ->
                            throw Exception("too many bytes")
                        else ->
                            deleteFile = false
                    }
                }
            }

            emit(Download.Finished(saveFile))
        } finally {
            // check if download was successful

            if (deleteFile) {
                saveFile.delete()
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()
package com.gabo.ramo.domain

import android.util.Log
import com.gabo.ramo.data.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.vicpin.krealmextensions.saveAll
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class FindMoviesByQueryInteractor(cacheDir: File?) : InteractorCommand<Response<List<Movie>>,String>{

    private val retrofit: Retrofit by lazy {

        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
        cacheDir?.let {
            retrofitBuilder.client(
                    OkHttpClient()
                            .newBuilder()
                            .cache(Cache(it, CACHE_SIZE_BYTES)).build())
        }

        retrofitBuilder.build()
    }

    override suspend fun execute(query: String): Response<List<Movie>> {
        try {
            val response = retrofit.create(MovieRepository::class.java).listMovieByQuery(query = query)
            val result = response.await()

            result.results?.let {
                it.saveAll()
            }

            return Response.Success(result.results)

        } catch (error: Throwable) {
            val errorMsg = "Error fetching movies for query ${query}"
            Log.e(FindMoviesByQueryInteractor::class.java.simpleName, errorMsg, error)
            return Response.Error(error, errorMsg)
        }
    }

}
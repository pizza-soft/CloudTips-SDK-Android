package ru.cloudtips.sdk.api

import androidx.annotation.NonNull
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.cloudtips.sdk.api.interfaces.TipsApi
import java.util.concurrent.TimeUnit

const val TIMEOUT: Long = 60;
const val WRITE_TIMEOUT: Long = 20;
const val CONNECT_TIMEOUT: Long = 10;

class ApiFactory {

    companion object {

        private val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val errorInterceptor = ErrorResponseInterceptor()

        private val client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(errorInterceptor)
            .build()

        private val gson = GsonBuilder()
            .setLenient()
            .create()

        @NonNull
        private fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(ApiEndPoint.getUrl())
                .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
        }

        // API implementations
        fun getTipsApi(): TipsApi {
            return getRetrofit().create(TipsApi::class.java)
        }
    }
}

class ErrorResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        val code = response.code

        if (code == 401) {

        } else if (code == 422) {  //} else if (code in 400..500) {
            response = response.newBuilder().code(200).build()
        }
        return response
    }
}
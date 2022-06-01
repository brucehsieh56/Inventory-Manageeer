package app.brucehsieh.inventorymanageeer.data

import app.brucehsieh.inventorymanageeer.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * An [OkHttpClient] singleton to run various HTTP requests.
 * */
object NetworkClient {

    val client by lazy {

        val okHttpClient = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC)

            okHttpClient.addInterceptor(loggingInterceptor)
        }
        okHttpClient.build()
    }
}
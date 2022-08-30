package app.brucehsieh.inventorymanageeer.common.di

import android.content.Context
import app.brucehsieh.inventorymanageeer.BuildConfig
import app.brucehsieh.inventorymanageeer.common.data.preferences.WalmartPreferences
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.AuthenticationInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.NetworkStatusInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.SocketTimeoutInterceptor
import app.brucehsieh.inventorymanageeer.common.utils.NetworkHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val socketTimeoutInterceptor by lazy { SocketTimeoutInterceptor() }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    @Provides
    @Singleton
    @HttpClientWalmart
    fun provideClientToWalmart(
        networkStatusInterceptor: NetworkStatusInterceptor,
        authenticationInterceptor: AuthenticationInterceptor,
    ): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(networkStatusInterceptor)
            .addInterceptor(socketTimeoutInterceptor)
            .addInterceptor(authenticationInterceptor)

        if (BuildConfig.DEBUG) {
            okHttpClient.addInterceptor(loggingInterceptor)
        }
        return okHttpClient.build()
    }

    @Provides
    @Singleton
    @HttpClientShopify
    fun provideClientShopify(networkStatusInterceptor: NetworkStatusInterceptor): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(networkStatusInterceptor)
            .addInterceptor(socketTimeoutInterceptor)

        if (BuildConfig.DEBUG) {
            okHttpClient.addInterceptor(loggingInterceptor)
        }
        return okHttpClient.build()
    }

    @Provides
    @Singleton
    fun provideNetworkStatusInterceptor(
        @ApplicationContext context: Context,
    ): NetworkStatusInterceptor {
        val networkHelper = NetworkHelper(context)
        return NetworkStatusInterceptor(networkHelper)
    }

    @Provides
    @Singleton
    fun provideAuthenticationInterceptorForWalmart(
        @ApplicationContext context: Context,
    ): AuthenticationInterceptor {
        val walmartPreferences = WalmartPreferences(context)
        return AuthenticationInterceptor(walmartPreferences)
    }
}
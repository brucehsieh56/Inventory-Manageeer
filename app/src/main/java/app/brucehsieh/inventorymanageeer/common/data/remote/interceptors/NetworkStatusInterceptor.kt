package app.brucehsieh.inventorymanageeer.common.data.remote.interceptors

import app.brucehsieh.inventorymanageeer.common.exception.NetworkUnavailableException
import app.brucehsieh.inventorymanageeer.common.utils.NetworkHelper
import okhttp3.Interceptor
import okhttp3.Response

class NetworkStatusInterceptor(private val networkHelper: NetworkHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (networkHelper.isNetworkAvailable()) {
            chain.proceed(chain.request())
        } else {
            throw NetworkUnavailableException()
        }
    }
}
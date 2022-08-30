package app.brucehsieh.inventorymanageeer.common.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException

/**
 * [Interceptor] to catch [SocketTimeoutException].
 * */
class SocketTimeoutInterceptor : Interceptor {

    private val retryTimes = 2
    private var counter = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (t: Throwable) {
            when (t) {
                is SocketTimeoutException -> {
                    counter += 1
                    if (counter <= retryTimes) {
                        chain.call().clone().execute()
                    } else {
                        // Reset counter
                        counter = 0
                        throw SocketTimeoutException()
                    }
                }
                else -> throw  t
            }
        }
    }
}
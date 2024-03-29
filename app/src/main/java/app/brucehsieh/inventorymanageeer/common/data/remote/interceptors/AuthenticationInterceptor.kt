package app.brucehsieh.inventorymanageeer.common.data.remote.interceptors

import app.brucehsieh.inventorymanageeer.common.data.preferences.Preferences
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.ACCEPT_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.ACCEPT_VALUE
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.AUTHORIZATION_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.CONTENT_TYPE_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.CONTENT_TYPE_VALUE
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.GRANT_TYPE_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.GRANT_TYPE_VALUE
import app.brucehsieh.inventorymanageeer.common.data.remote.dto.walmart.WalmartToken
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.AUTH_ENDPOINT
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.WALMART_ACCESS_TOKEN_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.WALMART_QOS_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.WALMART_QOS_VALUE
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.WALMART_SVC_NAME_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.WALMART_SVC_NAME_VALUE
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.threeten.bp.Instant

class AuthenticationInterceptor(private val preferences: Preferences) : Interceptor {

    companion object {
        const val UNAUTHORIZED = 401
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = preferences.getToken()
        val tokenExpirationTime = Instant.ofEpochSecond(preferences.getTokenExpirationTime())
        val request = chain.request()

        val interceptedRequest: Request
        if (tokenExpirationTime.isAfter(Instant.now())) {
            // Token is valid
            interceptedRequest = chain.createAuthenticatedRequest(token)
        } else {
            // Token expired
            val tokenRefreshResponse = chain.refreshToken()

            interceptedRequest = if (tokenRefreshResponse.isSuccessful) {
                val jsonString = tokenRefreshResponse.body!!.string()
                val newToken = mapToToken(jsonString)
                if (newToken.isValid()) {
                    storeNewToken(newToken)
                    chain.createAuthenticatedRequest(newToken.accessToken!!)
                } else {
                    request
                }
            } else {
                request
            }
        }

        return chain.proceedDeletingTokenIfUnauthorized(interceptedRequest)
    }

    private fun Interceptor.Chain.refreshToken(): Response {
        val url = request()
            .url
            .newBuilder(AUTH_ENDPOINT)!!
            .build()

        val body = FormBody.Builder()
            .add(GRANT_TYPE_KEY, GRANT_TYPE_VALUE)
            .build()

        val tokenRefresh = request()
            .newBuilder()
            .post(body)
            .url(url)
            .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
            .addHeader(ACCEPT_KEY, ACCEPT_VALUE)
            .addHeader(WALMART_SVC_NAME_KEY, WALMART_SVC_NAME_VALUE)
            .addHeader(WALMART_QOS_KEY, WALMART_QOS_VALUE)
            .addHeader(AUTHORIZATION_KEY, WalmartApiService.credential)
            .build()

        return proceedDeletingTokenIfUnauthorized(tokenRefresh)
    }

    private fun Interceptor.Chain.proceedDeletingTokenIfUnauthorized(request: Request): Response {
        val response = proceed(request)
        if (response.code == UNAUTHORIZED) {
            preferences.deleteTokenInfo()
        }
        return response
    }

    private fun Interceptor.Chain.createAuthenticatedRequest(token: String): Request {
        // Append token and credential to every request
        return request()
            .newBuilder()
            .addHeader(WALMART_ACCESS_TOKEN_KEY, token)
            .addHeader(AUTHORIZATION_KEY, WalmartApiService.credential)
            .build()
    }

    private fun mapToToken(jsonString: String): WalmartToken {
        return Gson().fromJson(jsonString, WalmartToken::class.java) ?: WalmartToken.INVALID
    }

    private fun storeNewToken(walmartToken: WalmartToken) {
        with(preferences) {
            putTokenType(walmartToken.tokenType!!)
            putTokenExpirationTime(walmartToken.expiresAt)
            putToken(walmartToken.accessToken!!)
        }
    }
}
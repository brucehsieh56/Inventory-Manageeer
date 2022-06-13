package app.brucehsieh.inventorymanageeer.data.remote.serviceapi

import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.data.NetworkClient
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.Quantity
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartInventory
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartItems
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartToken
import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "WalmartApiService"

/**
 * A singleton to run Walmart related requests.
 * */
object WalmartApiService {
    private const val BASE_URL = "https://marketplace.walmartapis.com/v3/"
    private const val WALMART_API_KEY = "YOUR_WALMART_API_KEY"
    private const val WALMART_API_SECRET = "YOUR_WALMART_API_SECRET"

    private val credential = Credentials.basic(WALMART_API_KEY, WALMART_API_SECRET)

    /**
     * Get token authentication.
     *
     * @param baseUrl this param is created for unit test purpose.
     * @return token in [String], which is empty if no token retrieved.
     * */
    suspend fun getToken(
        baseUrl: String = BASE_URL,
        key: String = WALMART_API_KEY,
        secret: String = WALMART_API_SECRET
    ): String {

        val credential = Credentials.basic(key, secret)

        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url("${baseUrl}token")
            .method("POST", body)
            .addHeader("grant_type", "client_credentials")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "application/json")
            .addHeader("WM_SVC.NAME", "Walmart Marketplace")
            .addHeader("WM_QOS.CORRELATION_ID", "b3261d2d-028a-4ef7-8602-633c23200af6")
            .addHeader("Authorization", credential)
            .build()

        return suspendCancellableCoroutine { continuation ->
            NetworkClient.client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.isSuccessful) {
                        true -> {
                            if (response.body != null) {
                                val walmartToken = Gson().fromJson(
                                    response.body!!.string(),
                                    WalmartToken::class.java
                                )
                                continuation.resume(walmartToken.accessToken)
                            } else {
                                continuation.resume(String.empty())
                            }
                        }
                        false -> {
                            val throwable = Failure.ServerError(
                                code = response.code,
                                message = response.body!!.string(),
                                description = when (response.code) {
                                    400 -> "Bad Request"
                                    401 -> "Unauthorized"
                                    else -> String.empty()
                                }
                            )
                            continuation.resumeWithException(throwable)
                        }
                    }
                }
            })
        }
    }

    /**
     * Get items.
     *
     * @return [WalmartItems].
     * */
    suspend fun getItems(): WalmartItems {

        val token = try {
            getToken()
        } catch (t: Throwable) {
            throw t
        }

        val request = Request.Builder()
            .url("${BASE_URL}items")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("WM_SVC.NAME", "Walmart Marketplace")
            .addHeader("WM_QOS.CORRELATION_ID", "b3261d2d-028a-4ef7-8602-633c23200af6")
            .addHeader("WM_SEC.ACCESS_TOKEN", token)
            .addHeader("Authorization", credential)
            .build()

        return suspendCancellableCoroutine { continuation ->
            NetworkClient.client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.isSuccessful) {
                        true -> {
                            if (response.body != null) {
                                val walmartItems = Gson().fromJson(
                                    response.body!!.string(),
                                    WalmartItems::class.java
                                )
                                continuation.resume(walmartItems)
                            } else {
                                continuation.resume(WalmartItems.empty)
                            }
                        }
                        false -> {
                            throw Failure.ServerError(
                                code = response.code,
                                message = response.body!!.string(),
                                description = when (response.code) {
                                    400 -> "Bad Request"
                                    401 -> "Unauthorized"
                                    else -> String.empty()
                                }
                            )
                        }
                    }
                }
            })
        }
    }

    /**
     * Get inventory for one item by sku.
     *
     * @param sku
     * @return [WalmartInventory]
     * */
    suspend fun getInventoryBySku(sku: String): WalmartInventory {

        val token = try {
            getToken()
        } catch (t: Throwable) {
            throw t
        }

        val request = Request.Builder()
            .url("${BASE_URL}inventory/?sku=$sku")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("WM_SVC.NAME", "Walmart Marketplace")
            .addHeader("WM_QOS.CORRELATION_ID", "b3261d2d-028a-4ef7-8602-633c23200af6")
            .addHeader("WM_SEC.ACCESS_TOKEN", token)
            .addHeader("Authorization", credential)
            .build()

        return suspendCancellableCoroutine { continuation ->
            NetworkClient.client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.isSuccessful) {
                        true -> {
                            if (response.body != null) {
                                val walmartInventory = Gson().fromJson(
                                    response.body!!.string(),
                                    WalmartInventory::class.java
                                )
                                continuation.resume(walmartInventory)
                            } else {
                                continuation.resume(WalmartInventory.empty)
                            }
                        }
                        false -> {
                            throw Failure.ServerError(
                                code = response.code,
                                message = response.body!!.string(),
                                description = when (response.code) {
                                    400 -> "Bad Request"
                                    401 -> "Unauthorized"
                                    else -> String.empty()
                                }
                            )
                        }
                    }
                }
            })
        }
    }

    /**
     * Update inventory for one item by sku.
     *
     * @param sku
     * @return Updated [WalmartInventory]
     * */
    suspend fun updateInventoryBySku(sku: String, newQuantity: Int): WalmartInventory {

        val token = try {
            getToken()
        } catch (t: Throwable) {
            throw t
        }

        val newWalmartInventory = WalmartInventory(
            quantity = Quantity(amount = newQuantity),
            sku = sku
        )

        val body = Gson().toJson(newWalmartInventory)
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${BASE_URL}inventory/?sku=$sku")
            .method("PUT", body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("WM_SVC.NAME", "Walmart Marketplace")
            .addHeader("WM_QOS.CORRELATION_ID", "b3261d2d-028a-4ef7-8602-633c23200af6")
            .addHeader("WM_SEC.ACCESS_TOKEN", token)
            .addHeader("Authorization", credential)
            .build()

        return suspendCancellableCoroutine { continuation ->
            NetworkClient.client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.isSuccessful) {
                        true -> {
                            if (response.body != null) {
                                val walmartInventory = Gson().fromJson(
                                    response.body!!.string(),
                                    WalmartInventory::class.java
                                )
                                continuation.resume(walmartInventory)
                            } else {
                                continuation.resume(WalmartInventory.empty)
                            }
                        }
                        false -> {
                            throw Failure.ServerError(
                                code = response.code,
                                message = response.body!!.string(),
                                description = when (response.code) {
                                    400 -> "Bad Request"
                                    401 -> "Unauthorized"
                                    else -> String.empty()
                                }
                            )
                        }
                    }
                }
            })
        }
    }
}

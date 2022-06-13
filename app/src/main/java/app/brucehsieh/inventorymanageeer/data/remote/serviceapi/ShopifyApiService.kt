package app.brucehsieh.inventorymanageeer.data.remote.serviceapi

import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.data.NetworkClient
import app.brucehsieh.inventorymanageeer.data.remote.dto.shopify.ShopifyItems
import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A singleton to run Shopify related requests.
 * */
object ShopifyApiService {
    private const val SHOPIFY_API_KEY = "SHOPIFY_API_KEY"
    private const val SHOPIFY_API_SECRET = "SHOPIFY_API_SECRET"
    private const val STORE_NAME = "STORE_NAME"

    private const val BASE_URL =
        "https://$SHOPIFY_API_KEY:$SHOPIFY_API_SECRET@$STORE_NAME.myshopify.com/admin/api/"

    /**
     * Get items.
     *
     * @return [ShopifyItems]
     * */
    suspend fun getItems(): ShopifyItems {
        val version = "2021-10"
        val resource = "products"

        val request = Request.Builder()
            .url("$BASE_URL/$version/$resource.json")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Shopify-Access-Token", SHOPIFY_API_SECRET)
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
                                val shopifyItems = Gson().fromJson(
                                    response.body!!.string(),
                                    ShopifyItems::class.java
                                )
                                continuation.resume(shopifyItems)
                            } else {
                                continuation.resume(ShopifyItems.empty)
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
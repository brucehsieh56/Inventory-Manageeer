package app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi

import app.brucehsieh.inventorymanageeer.common.functional.suspendRequestCall
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.ACCEPT_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.ACCEPT_VALUE
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.CONTENT_TYPE_KEY
import app.brucehsieh.inventorymanageeer.common.data.remote.ApiParameters.CONTENT_TYPE_VALUE
import app.brucehsieh.inventorymanageeer.common.data.remote.dto.walmart.Quantity
import app.brucehsieh.inventorymanageeer.common.data.remote.dto.walmart.WalmartInventory
import app.brucehsieh.inventorymanageeer.common.data.remote.dto.walmart.WalmartItems
import app.brucehsieh.inventorymanageeer.common.extension.empty
import com.google.gson.Gson
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "WalmartApiService"

/**
 * A singleton to run Walmart related requests.
 * */
class WalmartApiService(private val client: OkHttpClient) {
    companion object {
        const val BASE_URL = "https://marketplace.walmartapis.com/v3/"
        const val AUTH_ENDPOINT = "token"
        const val ITEMS_ENDPOINT = "items"
        const val TOKEN_TYPE = "Bearer"
        const val WALMART_SVC_NAME_KEY = "WM_SVC.NAME"
        const val WALMART_SVC_NAME_VALUE = "Walmart Marketplace"
        const val WALMART_QOS_KEY = "WM_QOS.CORRELATION_ID"
        const val WALMART_QOS_VALUE = "b3261d2d-028a-4ef7-8602-633c23200af6"
        const val WALMART_ACCESS_TOKEN_KEY = "WM_SEC.ACCESS_TOKEN"
        var WALMART_API_KEY = String.empty()
        var WALMART_API_SECRET = String.empty()
        val credential get() = Credentials.basic(WALMART_API_KEY, WALMART_API_SECRET)
    }

    /**
     * Set Walmart API key.
     * */
    fun setKey(key: String) {
        WALMART_API_KEY = key
    }

    /**
     * Set Walmart API secret
     * */
    fun setSecret(secret: String) {
        WALMART_API_SECRET = secret
    }

    /**
     * Get items.
     *
     * @return [WalmartItems].
     * */
    suspend fun getItems(): WalmartItems {

        if (WALMART_API_KEY.isEmpty() || WALMART_API_SECRET.isEmpty()) return WalmartItems.empty

        val request = Request.Builder()
            .url("${BASE_URL}items")
            .method("GET", null)
            .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
            .addHeader(ACCEPT_KEY, ACCEPT_VALUE)
            .addHeader(WALMART_SVC_NAME_KEY, WALMART_SVC_NAME_VALUE)
            .addHeader(WALMART_QOS_KEY, WALMART_QOS_VALUE)
            .build()

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartItems::class.java)
            },
            default = WalmartItems.empty,
            client = client
        )
    }

    /**
     * Get inventory for one item by sku.
     *
     * @param sku
     * @return [WalmartInventory]
     * */
    suspend fun getInventoryBySku(sku: String): WalmartInventory {
        val request = Request.Builder()
            .url("${BASE_URL}inventory/?sku=$sku")
            .method("GET", null)
            .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
            .addHeader(ACCEPT_KEY, ACCEPT_VALUE)
            .addHeader(WALMART_SVC_NAME_KEY, WALMART_SVC_NAME_VALUE)
            .addHeader(WALMART_QOS_KEY, WALMART_QOS_VALUE)
            .build()

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartInventory::class.java)
            },
            default = WalmartInventory.empty,
            client = client
        )
    }

    /**
     * Update inventory for one item by sku.
     *
     * @param sku
     * @return Updated [WalmartInventory]
     * */
    suspend fun updateInventoryBySku(sku: String, newQuantity: Int): WalmartInventory {
        val newWalmartInventory = WalmartInventory(
            quantity = Quantity(amount = newQuantity),
            sku = sku
        )

        val body = Gson().toJson(newWalmartInventory)
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${BASE_URL}inventory/?sku=$sku")
            .method("PUT", body)
            .addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
            .addHeader(ACCEPT_KEY, ACCEPT_VALUE)
            .addHeader(WALMART_SVC_NAME_KEY, WALMART_SVC_NAME_VALUE)
            .addHeader(WALMART_QOS_KEY, WALMART_QOS_VALUE)
            .build()

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartInventory::class.java)
            },
            default = WalmartInventory.empty,
            client = client
        )
    }
}

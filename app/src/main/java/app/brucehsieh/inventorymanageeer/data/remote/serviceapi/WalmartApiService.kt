package app.brucehsieh.inventorymanageeer.data.remote.serviceapi

import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.common.functional.suspendRequestCall
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.Quantity
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartInventory
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartItems
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartToken
import com.google.gson.Gson
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "WalmartApiService"

/**
 * A singleton to run Walmart related requests.
 * */
object WalmartApiService {
    private const val BASE_URL = "https://marketplace.walmartapis.com/v3/"
    private var WALMART_API_KEY = String.empty()
    private var WALMART_API_SECRET = String.empty()

    private lateinit var token: String
    private val credential get() = Credentials.basic(WALMART_API_KEY, WALMART_API_SECRET)

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

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartToken::class.java).accessToken
            },
            default = String.empty()
        )
    }

    /**
     * Get items.
     *
     * @return [WalmartItems].
     * */
    suspend fun getItems(): WalmartItems {

        if (WALMART_API_KEY.isEmpty() || WALMART_API_SECRET.isEmpty()) return WalmartItems.empty

        token = try {
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

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartItems::class.java)
            },
            default = WalmartItems.empty
        )
    }

    /**
     * Get inventory for one item by sku.
     *
     * @param sku
     * @return [WalmartInventory]
     * */
    suspend fun getInventoryBySku(sku: String): WalmartInventory {

//        val token = try {
//            getToken()
//        } catch (t: Throwable) {
//            throw t
//        }

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

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartInventory::class.java)
            },
            default = WalmartInventory.empty
        )
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

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, WalmartInventory::class.java)
            },
            default = WalmartInventory.empty
        )
    }
}

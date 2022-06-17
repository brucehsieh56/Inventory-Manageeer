package app.brucehsieh.inventorymanageeer.data.remote.serviceapi

import app.brucehsieh.inventorymanageeer.common.functional.suspendRequestCall
import app.brucehsieh.inventorymanageeer.data.remote.dto.shopify.InventoryLevel
import app.brucehsieh.inventorymanageeer.data.remote.dto.shopify.PostInventoryLevel
import app.brucehsieh.inventorymanageeer.data.remote.dto.shopify.ShopifyInventoryLevel
import app.brucehsieh.inventorymanageeer.data.remote.dto.shopify.ShopifyItems
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, ShopifyItems::class.java)
            },
            default = ShopifyItems.empty
        )
    }

    /**
     * Get single item inventory. The goal is to get [locationId] inside [ShopifyInventoryLevel],
     * which is required to update inventory.
     *
     * @return [ShopifyInventoryLevel]
     * */
    suspend fun getSingleInventory(inventoryItemId: Long): ShopifyInventoryLevel {
        val version = "2022-04"
        val resource = "inventory_levels"

        val request = Request.Builder()
            .url("$BASE_URL/$version/$resource.json?inventory_item_ids=$inventoryItemId")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Shopify-Access-Token", SHOPIFY_API_SECRET)
            .build()
        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, ShopifyInventoryLevel::class.java)
            },
            default = ShopifyInventoryLevel.empty
        )
    }

    /**
     * Update single item inventory.
     *
     * @return [PostInventoryLevel] instead of [ShopifyInventoryLevel]
     * */
    suspend fun updateSingleInventory(
        inventoryItemId: Long,
        locationId: Long,
        newQuantity: Int
    ): PostInventoryLevel {
        val version = "2022-04"
        val resource = "inventory_levels/set"

        val inventoryLevel = InventoryLevel.empty.copy(
            available = newQuantity,
            inventoryItemId = inventoryItemId,
            locationId = locationId
        )

        val body = Gson().toJson(inventoryLevel)
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$BASE_URL$version/$resource.json")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Shopify-Access-Token", SHOPIFY_API_SECRET)
            .build()

        return suspendRequestCall(
            request = request,
            transform = { jsonString ->
                Gson().fromJson(jsonString, PostInventoryLevel::class.java)
            },
            default = PostInventoryLevel.empty
        )
    }
}
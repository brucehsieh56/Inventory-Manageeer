package app.brucehsieh.inventorymanageeer.common.data.remote.dto.shopify

import app.brucehsieh.inventorymanageeer.common.extension.empty
import com.google.gson.annotations.SerializedName

/**
 * DTO returned from Shopify GET inventory.
 * */
data class ShopifyInventoryLevel(
    val inventory_levels: List<InventoryLevel>
) {
    companion object {
        val empty = ShopifyInventoryLevel(emptyList())
    }
}

/**
 * DTO returned from Shopify after POST / updating inventory.
 * */
data class PostInventoryLevel(
    val inventory_level: InventoryLevel
) {
    companion object {
        val empty = PostInventoryLevel(InventoryLevel.empty)
    }
}

/**
 * Data class contains inventory related variables.
 *
 * @param available is the quantity of a listing item.
 * @param inventoryItemId is the unique ID of a listing item.
 * */
data class InventoryLevel(
    val admin_graphql_api_id: String,
    val available: Int,
    @SerializedName("inventory_item_id") val inventoryItemId: Long,
    @SerializedName("location_id") val locationId: Long,
    val updated_at: String
) {
    companion object {
        val empty = InventoryLevel(String.empty(), -1, -1L, -1L, String.empty())
    }
}


package app.brucehsieh.inventorymanageeer.data.remote.dto.shopify

import com.google.gson.annotations.SerializedName

/**
 * DTO for items from Shopify Marketplace api.
 * */
data class ShopifyItems(
    val products: List<Product>
) {
    companion object {
        val empty = ShopifyItems(emptyList())
    }
}

data class Product(
    val admin_graphql_api_id: String,
    val body_html: String,
    val created_at: String,
    val handle: String,
    val id: Long,
    val image: Image,
    val images: List<Image>,
    val options: List<Option>,
    val product_type: String,
    val published_at: String,
    val published_scope: String,
    val status: String,
    val tags: String,
    val template_suffix: String,
    val title: String,
    val updated_at: String,
    val variants: List<Variant>,
    val vendor: String
)

data class Image(
    val admin_graphql_api_id: String,
    val alt: Any,
    val created_at: String,
    val height: Int,
    val id: Long,
    val position: Int,
    val product_id: Long,
    val src: String,
    val updated_at: String,
    val variant_ids: List<Long>,
    val width: Int
)

data class Option(
    val id: Long,
    val name: String,
    val position: Int,
    val product_id: Long,
    val values: List<String>
)

data class Variant(
    val admin_graphql_api_id: String,
    val barcode: String,
    val compare_at_price: Any,
    val created_at: String,
    val fulfillment_service: String,
    val grams: Int,
    val id: Long,
    val image_id: Any,
    @SerializedName("inventory_item_id") val inventoryItemId: Long,
    val inventory_management: String,
    val inventory_policy: String,
    @SerializedName("inventory_quantity") val inventoryQuantity: Int,
    val old_inventory_quantity: Int,
    val option1: String,
    val option2: Any,
    val option3: Any,
    val position: Int,
    val price: String,
    val product_id: Long,
    val requires_shipping: Boolean,
    val sku: String,
    val taxable: Boolean,
    val title: String,
    val updated_at: String,
    val weight: Double,
    val weight_unit: String
)
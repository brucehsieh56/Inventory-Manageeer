package app.brucehsieh.inventorymanageeer.common.domain.model

/**
 * A data class holds Shopify listing data that is ready for UI.
 * */
data class ShopifyListing(
    override val productName: String,
    override val productSku: String,
    override val quantity: Int,
    override val price: Float,
    override val imageUrl: String? = null,

    // Unique to Shopify listing
    val inventoryItemId: Long? = null
) : BaseListing
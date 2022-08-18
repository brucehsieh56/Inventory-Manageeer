package app.brucehsieh.inventorymanageeer.common.domain.model

/**
 * A data class holds Walmart listing data that is ready for UI.
 * */
data class WalmartListing(
    override val productName: String,
    override val productSku: String,
    override val quantity: Int,
    override val price: Float,
    override val imageUrl: String? = null
) : BaseListing
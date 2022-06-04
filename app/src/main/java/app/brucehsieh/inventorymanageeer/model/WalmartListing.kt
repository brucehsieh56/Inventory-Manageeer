package app.brucehsieh.inventorymanageeer.model

/**
 * A data class holds Walmart listing data that is ready for UI.
 * */
data class WalmartListing(
    val productName: String,
    val productSku: String,
    val quantity: Int,
    val price: Float,
    val imageUrl: String? = null
)

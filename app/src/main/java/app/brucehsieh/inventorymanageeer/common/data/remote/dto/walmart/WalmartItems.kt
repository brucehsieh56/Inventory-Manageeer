package app.brucehsieh.inventorymanageeer.common.data.remote.dto.walmart

/**
 * DTO for items from Walmart Marketplace api.
 * */
data class WalmartItems(
    val ItemResponse: List<ItemResponse>,
    val totalItems: Int
) {
    companion object {
        val empty = WalmartItems(ItemResponse = emptyList(), totalItems = 0)
    }
}

/**
 * Single item.
 * */
data class ItemResponse(
    val gtin: String,
    val lifecycleStatus: String,
    val mart: String,
    val price: Price,
    val productName: String,
    val productType: String,
    val publishedStatus: String,
    val shelf: String,
    val sku: String,
    val unpublishedReasons: UnpublishedReasons,
    val upc: String,
    val wpid: String
)

data class Price(
    val amount: Double,
    val currency: String
)

data class UnpublishedReasons(
    val reason: List<String>
)